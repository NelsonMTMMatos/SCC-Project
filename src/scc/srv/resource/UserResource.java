package scc.srv.resource;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import org.mindrot.jbcrypt.BCrypt;
import redis.clients.jedis.Jedis;
import scc.authentication.Login;
import scc.authentication.Session;
import scc.cache.RedisCache;
import scc.data.HouseDAO;
import scc.data.User;
import scc.data.UserDAO;
import scc.db.CosmosDBLayer;
import scc.utils.Helpers;



import java.util.*;
import java.util.stream.Collectors;

@Path("/users")
public class UserResource {

    private final String ID = "id";
    private final String USER_CACHE_ENTRY_FORMAT = "user:%s";
    private final String OWNER_CACHE_ENTRY_FORMAT = "user:%s:houses";
    private final CosmosDBLayer db;
    public UserResource(){
        db = CosmosDBLayer.getInstance();
    }

    @POST
    @Path("/auth")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response auth(Login user) {
        try(Jedis jedis = RedisCache.getCachePool().getResource()) {
            UserDAO userDAO = existentUser(jedis, user.getUsername());

            boolean pwdOK = BCrypt.checkpw(user.getPassword(), userDAO.getPwd());

            if (pwdOK) {
                String uid = UUID.randomUUID().toString();
                NewCookie cookie = new NewCookie.Builder("scc:session")
                        .value(uid)
                        .path("/")
                        .comment("sessionid")
                        .maxAge(3600)
                        .secure(false)
                        .httpOnly(true)
                        .build();
                RedisCache.putSession(new Session(uid, user.getUsername()));
                return Response.ok().cookie(cookie).build();
            } else
                throw new NotAuthorizedException("Incorrect login");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response createUser(User user){
        try(Jedis jedis = RedisCache.getCachePool().getResource()){
            String userIdInCache = String.format(USER_CACHE_ENTRY_FORMAT, user.getId());
            String res = jedis.get(userIdInCache);

            if(res != null) return Response.status(Status.CONFLICT).build();

            UserDAO newUser = new UserDAO(user);

            db.createUser(newUser);

            jedis.set(userIdInCache, new ObjectMapper().writeValueAsString(newUser));

            return Response.ok(newUser.getId()).build();

        } catch (CosmosException e) {
            if (e.getStatusCode() == 409)
                return Response.status(Response.Status.CONFLICT).build();
        }catch (Exception e){
            e.printStackTrace();
        }

        return Response.serverError().build();
    }

    @DELETE
    @Path("/{"+ ID + "}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUser(@PathParam(ID) String id){
        try(Jedis jedis = RedisCache.getCachePool().getResource()){
            db.delUserById(id).getItem();
            jedis.del(String.format(USER_CACHE_ENTRY_FORMAT, id));

            return Response.ok().build();

        } catch (CosmosException e){
            if (e.getStatusCode() == 404)
                return Response.status(Response.Status.NOT_FOUND).build();
        }catch (Exception e){
            e.printStackTrace();
        }

        return Response.serverError().build();
    }

    @PUT
    @Path("/{"+ ID + "}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUser(@CookieParam("scc:session") Cookie session, @PathParam(ID) String id, User user){
        try(Jedis jedis = RedisCache.getCachePool().getResource()){

            Session s = Helpers.checkCookieUser(session, id);

            if(s == null)
                return Response.status(Status.UNAUTHORIZED).build();

            if(!s.getUser().equals(user.getId()))
                return Response.status(Status.FORBIDDEN).build();

            UserDAO uDao = db.updateUser(new UserDAO(user)).getItem();
            jedis.set(String.format(USER_CACHE_ENTRY_FORMAT, id), new ObjectMapper().writeValueAsString(uDao));

            return Response.ok(user).build();

        } catch (CosmosException e) {
            if (e.getStatusCode() == 404)
                return Response.status(Response.Status.NOT_FOUND).build();
        }catch (Exception e){
            e.printStackTrace();
        }

        return Response.serverError().build();
    }

    @GET
    @Path("/{"+ ID + "}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserById(@PathParam(ID) String id){
        try(Jedis jedis = RedisCache.getCachePool().getResource()){
            UserDAO uDao = existentUser(jedis, id);

            jedis.set(String.format(USER_CACHE_ENTRY_FORMAT, id), new ObjectMapper().writeValueAsString(uDao));

            return Response.ok(uDao.toUser()).build();

        } catch (CosmosException e){
            if (e.getStatusCode() == 404) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return Response.serverError().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUsers(){
        return Response.ok(db.getUsers()
                .stream()
                .collect(Collectors.toList()))
                .build();
    }

    @GET
    @Path("/{" + ID + "}/houses")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getHouses(@PathParam(ID) String id){
        try(Jedis jedis = RedisCache.getCachePool().getResource()){
            existentUser(jedis, id);

            String housesInCache = String.format(OWNER_CACHE_ENTRY_FORMAT, id);
            String res = jedis.get(housesInCache);
            ObjectMapper mapper = new ObjectMapper();

            if(res != null)
                return Response.ok(mapper.readValue(res, List.class)).build();

            List<HouseDAO> houses = db.getHousesOfUser(id).stream().collect(Collectors.toList());

            jedis.set(housesInCache, mapper.writeValueAsString(houses));

            return Response.ok(houses).build();

        } catch (CosmosException e){
            if (e.getStatusCode() == 404) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return Response.serverError().build();
    }

    private UserDAO existentUser(Jedis jedis, String id) throws JsonProcessingException {
        String userIdInCache = String.format(USER_CACHE_ENTRY_FORMAT, id);
        String res = jedis.get(userIdInCache);

        return res != null ? new ObjectMapper().readValue(res, UserDAO.class) : db.getUserById(id).getItem();
    }
}
