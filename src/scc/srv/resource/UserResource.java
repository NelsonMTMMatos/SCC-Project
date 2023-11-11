package scc.srv.resource;

import com.azure.cosmos.util.CosmosPagedIterable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
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


import java.util.*;
import java.util.stream.Collectors;

@Path("/user")
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


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response createUser(User user){
        try(Jedis jedis = RedisCache.getCachePool().getResource()){
            String userIdInCache = String.format(USER_CACHE_ENTRY_FORMAT, user.getId());
            String res = jedis.get(userIdInCache);

            if(res != null) throw new Exception("User already exists.");

            CosmosPagedIterable<UserDAO> resGet = db.getUserById(user.getId());
            UserDAO uDao = Helpers.getItem(resGet);

            if(uDao != null) return Response.status(Status.CONFLICT).build();

            UserDAO newUser = new UserDAO(user);

            db.createUser(newUser);
            jedis.set(userIdInCache, Helpers.serialize(newUser));

            return Helpers.ok(newUser.getId());
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    @DELETE
    @Path("/{"+ ID + "}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUser(@PathParam(ID) String id){
        try(Jedis jedis = RedisCache.getCachePool().getResource()){
            UserDAO uDao = (UserDAO) db.delUserById(id).getItem();

            if(uDao == null)
                return Response.status(Status.NOT_FOUND).build();

            jedis.del(String.format(USER_CACHE_ENTRY_FORMAT, id));

            return Helpers.ok(uDao.toUser());
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    @PUT
    @Path("/{"+ ID + "}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUser(@PathParam(ID) String id, User user){
        try(Jedis jedis = RedisCache.getCachePool().getResource()){
            UserDAO uDao = db.updateUser(new UserDAO(user)).getItem();

            if(uDao == null)
                return Response.status(Status.NOT_FOUND).build();

            jedis.set(String.format(USER_CACHE_ENTRY_FORMAT, id), Helpers.serialize(uDao));
            return Helpers.ok(user);
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    @GET
    @Path("/{"+ ID + "}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserById(@PathParam(ID) String id){
        try(Jedis jedis = RedisCache.getCachePool().getResource()){
            UserDAO uDao = checkIfUserExist(jedis, id);

            if(uDao == null)
                return Response.status(Status.NOT_FOUND).build();

            jedis.set(String.format(USER_CACHE_ENTRY_FORMAT, id), Helpers.serialize(uDao));

            return Helpers.ok(uDao.toUser());
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUsers(){
        return Helpers.ok(db.getUsers());
    }

    @GET
    @Path("/{" + ID + "}/houses")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getHouses(@PathParam(ID) String id){
        try(Jedis jedis = RedisCache.getCachePool().getResource()){
            UserDAO uDao = checkIfUserExist(jedis, id);

            if(uDao == null)
                return Response.status(Status.NOT_FOUND).build();

            CosmosPagedIterable<HouseDAO> houses = db.getHousesOfUser(id);
            String housesInCache = String.format(OWNER_CACHE_ENTRY_FORMAT, id);

            jedis.set(housesInCache, Helpers.serialize(houses.stream().collect(Collectors.toList())));

            /*
            Set<House> houses = new HashSet<>();
            for(String hID: uDao.getHouseIds()) {
                ObjectMapper mapper = new ObjectMapper();
                String houseIdInCache = String.format(HouseResource.HOUSE_CACHE_ENTRY_FORMAT, hID);
                String res = jedis.get(houseIdInCache);

                House h = mapper.readValue(res, HouseDAO.class).toHouse();

                if(h == null) {
                    h = db.getHouseById(hID).stream().iterator().next().toHouse();
                    jedis.set(houseIdInCache, Helpers.serialize(h));
                }
                houses.add(h);
            }
             */

            return Helpers.ok(db.getHousesOfUser(id).toString());
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    private UserDAO checkIfUserExist(Jedis jedis, String id) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String userIdInCache = String.format(USER_CACHE_ENTRY_FORMAT, id);
        String res = jedis.get(userIdInCache);
        UserDAO uDao;

        if(res != null)
            uDao = mapper.readValue(res, UserDAO.class);
        else{
            CosmosPagedIterable<UserDAO> resGet = db.getUserById(id);
            uDao = Helpers.getItem(resGet);
        }

        return uDao;
    }
}
