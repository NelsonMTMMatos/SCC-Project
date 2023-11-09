package scc.srv.resource;

import com.azure.cosmos.util.CosmosPagedIterable;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import redis.clients.jedis.Jedis;
import scc.authentication.Login;
import scc.authentication.Session;
import scc.cache.RedisCache;
import scc.data.House;
import scc.data.HouseDAO;
import scc.data.User;
import scc.data.UserDAO;
import scc.db.CosmosDBLayer;
import scc.utils.Helpers;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;

@Path("/user")
public class UserResource {

    private final String ID = "id";
    private final String PWD = "pwd";
    private final String USER_CACHE_ENTRY_FORMAT = "user:%s";
    public UserResource(){}

    @POST
    @Path("/auth")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response auth(Login user) {
        //TODO: Check if user exist in cache or db
        boolean pwdOk = false;
        //Check if user has password.
        //boolean pwdOK = BCrypt.checkpw(plainTextPassword, hashedPassword);
        if (pwdOk) {
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
    }

    @Path("/")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String createUser(User user){
        try(Jedis jedis = RedisCache.getCachePool().getResource()){
            String userIdInCache = String.format(USER_CACHE_ENTRY_FORMAT, user.getId());
            String res = jedis.get(userIdInCache);

            if(res != null) throw new Exception("User already exists.");

            CosmosDBLayer db = CosmosDBLayer.getInstance();
            CosmosPagedIterable<UserDAO> resGet = db.getUserById(user.getId());
            UserDAO uDao = Helpers.getItem(resGet);

            if(uDao != null) throw new Exception("User already exists.");

            UserDAO newUser = new UserDAO(user);

            db.createUser(newUser);
            jedis.set(userIdInCache, new ObjectMapper().writeValueAsString(newUser));

            return newUser.getId();
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    @DELETE
    @Path("/{"+ ID + "}")
    @Produces(MediaType.APPLICATION_JSON)
    public User deleteUser(@PathParam(ID) String id, @QueryParam(PWD) String pwd){
            try(Jedis jedis = RedisCache.getCachePool().getResource()){
                CosmosDBLayer db = CosmosDBLayer.getInstance();
                CosmosPagedIterable<UserDAO> resGet = db.getUserById(id);
                UserDAO uDao = Helpers.getItem(resGet);

                if(uDao == null)
                    throw new Exception("User does not exists.");

                if(!uDao.getPwd().equals(pwd))
                    throw new Exception("Password does not match.");

                db.delUserById(id);
                jedis.del(String.format(USER_CACHE_ENTRY_FORMAT, id));

                return uDao.toUser();
            }catch (Exception e){
                e.printStackTrace();
            }
        return null;
    }

    @PUT
    @Path("/{"+ ID + "}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public User updateUser(@PathParam(ID) String id, @QueryParam(PWD) String pwd, User user){
        try(Jedis jedis = RedisCache.getCachePool().getResource()){
            CosmosDBLayer db = CosmosDBLayer.getInstance();
            CosmosPagedIterable<UserDAO> resGet = db.getUserById(id);
            UserDAO uDao = Helpers.getItem(resGet);

            if(uDao == null)
                throw new Exception("User does not exists.");

            if(!uDao.getPwd().equals(pwd))
                throw new Exception("Password does not match.");

            UserDAO newUser = new UserDAO(user);

            db.updateUser(newUser);
            jedis.set(String.format(USER_CACHE_ENTRY_FORMAT, id), new ObjectMapper().writeValueAsString(newUser));

            return user;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @GET
    @Path("/{"+ ID + "}")
    @Produces(MediaType.APPLICATION_JSON)
    public User getUserById(@PathParam(ID) String id, @QueryParam(PWD) String pwd){
        try(Jedis jedis = RedisCache.getCachePool().getResource()){
            ObjectMapper mapper = new ObjectMapper();
            String userIdInCache = String.format(USER_CACHE_ENTRY_FORMAT, id);
            String res = jedis.get(userIdInCache);
            UserDAO uDao;

            if(res != null)
                uDao = mapper.readValue(res, UserDAO.class);
            else{
                CosmosDBLayer db = CosmosDBLayer.getInstance();
                CosmosPagedIterable<UserDAO> resGet = db.getUserById(id);
                uDao = Helpers.getItem(resGet);

                if(uDao == null)
                    throw new Exception("User does not exists.");
            }

            if (!uDao.getPwd().equals(pwd))
                throw new Exception("Password does not match.");

            jedis.set(userIdInCache, mapper.writeValueAsString(uDao));

            return uDao.toUser();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @GET
    @Path("/{" + ID + "}/houses")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Set<House> getHouses(@PathParam(ID) String id){
        try(Jedis jedis = RedisCache.getCachePool().getResource()){
            CosmosDBLayer db = CosmosDBLayer.getInstance();
            CosmosPagedIterable<UserDAO> resGet = db.getUserById(id);
            UserDAO u = Helpers.getItem(resGet);

            if(u == null)
                throw new Exception("User does not exists.");

            Set<House> houses = new HashSet<>();
            for(String hID: u.getHouseIds()) {
                ObjectMapper mapper = new ObjectMapper();
                String houseIdInCache = String.format(HouseResource.HOUSE_CACHE_ENTRY_FORMAT, hID);
                String res = jedis.get(houseIdInCache);

                House h = mapper.readValue(res, HouseDAO.class).toHouse();

                if(h == null) {
                    h = db.getHouseById(hID).stream().iterator().next().toHouse();
                    jedis.set(houseIdInCache, mapper.writeValueAsString(h));
                }

                houses.add(h);
            }

            return houses;
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }
}
