package scc.srv.resource;

import com.azure.cosmos.CosmosException;
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
import scc.db.CosmosDBLayerAsync;
import scc.utils.Helpers;

import java.util.List;
import java.util.UUID;

@Path("/users")
public class UserResource {

    private final String ID = "id";
    private final String USER_CACHE_ENTRY_FORMAT = "user:%s";
    private final String OWNER_CACHE_ENTRY_FORMAT = "user:%s:houses";
    private final String DELETED_USER = "Deleted User";
    private final CosmosDBLayer db;
    private final CosmosDBLayerAsync dbAsync;

    public UserResource(){
        db = CosmosDBLayer.getInstance();
        dbAsync = CosmosDBLayerAsync.getInstance();
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
    public Response createUser(User user){

        try{
            db.createUser(new UserDAO(user));
        } catch (CosmosException e) {
            if (e.getStatusCode() == 409)
                return Response.status(Status.CONFLICT).build();
        }

        return Response.ok().build();
    }

    @DELETE
    @Path("/{"+ ID + "}")
    public Response deleteUser(@CookieParam("scc:session") Cookie session, @PathParam(ID) String id){

        if(Helpers.checkCookieUser(session, id) == null && Helpers.AUTH_ON)
            return Response.status(Status.UNAUTHORIZED).build();

        try(Jedis jedis = RedisCache.getCachePool().getResource()){
            db.delUserById(id).getItem();
            jedis.del(String.format(USER_CACHE_ENTRY_FORMAT, id));

            db.getHousesOfUser(id).stream().forEach( hDao -> {
                hDao.setOwnerId(DELETED_USER);
                dbAsync.replaceHouse(hDao);
            });

            db.getRentalsOfUser(id).stream().forEach( rDao -> {
                rDao.setUserId(DELETED_USER);
                dbAsync.replaceRental(rDao);
            });

            jedis.del(String.format(RedisCache.SESSION_CACHE_ENTRY_FORMAT, session.getValue()));

            return Response.ok().build();
        } catch (CosmosException e){
            if (e.getStatusCode() == 404)
                return Response.status(Status.NOT_FOUND).build();
        }catch (Exception e){
            e.printStackTrace();
        }

        return Response.serverError().build();
    }

    @PUT
    @Path("/{"+ ID + "}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateUser(@CookieParam("scc:session") Cookie session, @PathParam(ID) String id, User user){

        if(Helpers.checkCookieUser(session, id) == null && Helpers.AUTH_ON)
            return Response.status(Status.UNAUTHORIZED).build();

        if(!user.getId().equals(id))
            return Response.status(Status.FORBIDDEN).build();

        try {
            db.updateUser(new UserDAO(user));
        }catch (CosmosException e) {
            if (e.getStatusCode() == 404)
                return Response.status(Status.NOT_FOUND).build();
        }

        return Response.ok().build();
    }

    @GET
    @Path("/{"+ ID + "}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserById(@PathParam(ID) String id){
        try(Jedis jedis = RedisCache.getCachePool().getResource()){
            UserDAO uDao = existentUser(jedis, id);

            String idInCache = String.format(USER_CACHE_ENTRY_FORMAT, id);
            jedis.set(idInCache, new ObjectMapper().writeValueAsString(uDao));
            jedis.expire(idInCache, 120);

            return Response.ok(uDao.toUser()).build();
        } catch (CosmosException e){
            if (e.getStatusCode() == 404) {
                return Response.status(Status.NOT_FOUND).build();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return Response.serverError().build();
    }

    @GET
    @Path("/{" + ID + "}/houses")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHouses(@PathParam(ID) String id){
        try(Jedis jedis = RedisCache.getCachePool().getResource()){
            existentUser(jedis, id);

            String housesInCache = String.format(OWNER_CACHE_ENTRY_FORMAT, id);
            String res = jedis.get(housesInCache);
            ObjectMapper mapper = new ObjectMapper();

            if(res != null)
                return Response.ok(mapper.readValue(res, List.class)).build();

            List<HouseDAO> houses = db.getHousesOfUser(id).stream().toList();

            jedis.set(housesInCache, mapper.writeValueAsString(houses));
            jedis.expire(housesInCache, 15);

            return Response.ok(houses).build();
        } catch (CosmosException e){
            if (e.getStatusCode() == 404)
                return Response.status(Status.NOT_FOUND).build();
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
