package scc.srv.resource;

import com.azure.cosmos.util.CosmosPagedIterable;
import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.Jedis;
import scc.cache.RedisCache;
import scc.data.House;
import scc.data.HouseDAO;
import scc.data.User;
import scc.data.UserDAO;
import scc.db.CosmosDBLayer;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;

@Path("/user")
public class UserResource {

    private final String ID = "id";
    private final String PWD = "pwd";
    private final String USER_CACHE_ENTRY_FORMAT = "user:%s";
    public UserResource(){}
    @Path("/")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String createUser(User user){
        try(Jedis jedis = RedisCache.getCachePool().getResource()){
            String res = jedis.get(String.format(USER_CACHE_ENTRY_FORMAT, user.getId()));

            if(res != null) throw new Exception("User already exists.");

            CosmosDBLayer db = CosmosDBLayer.getInstance();
            CosmosPagedIterable<UserDAO> resGet = db.getUserById(user.getId());
            UserDAO uDao = getUser(resGet);

            if(uDao != null) throw new Exception("User already exists.");

            UserDAO newUser = new UserDAO(user);

            db.createUser(newUser);
            jedis.set(String.format(USER_CACHE_ENTRY_FORMAT, user.getId()), new ObjectMapper().writeValueAsString(newUser));

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
                UserDAO uDao = getUser(resGet);

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
            UserDAO uDao = getUser(resGet);

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
            String res = jedis.get(String.format(USER_CACHE_ENTRY_FORMAT, id));
            UserDAO uDao;

            if(res != null)
                uDao = mapper.readValue(res, UserDAO.class);
            else{
                CosmosDBLayer db = CosmosDBLayer.getInstance();
                CosmosPagedIterable<UserDAO> resGet = db.getUserById(id);
                uDao = getUser(resGet);

                if(uDao == null)
                    throw new Exception("User does not exists.");
            }

            if (!uDao.getPwd().equals(pwd))
                throw new Exception("Password does not match.");

            jedis.set(String.format(USER_CACHE_ENTRY_FORMAT, id), mapper.writeValueAsString(uDao));

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
            UserDAO u = getUser(resGet);

            if(u == null)
                throw new Exception("User does not exists.");

            Set<House> houses = new HashSet<>();
            for(String hID: u.getHouseIds()) {
                ObjectMapper mapper = new ObjectMapper();
                String res = jedis.get(String.format(HouseResource.HOUSE_CACHE_ENTRY_FORMAT, hID));

                House h = mapper.readValue(res, HouseDAO.class).toHouse();

                if(h == null) {
                    h = db.getHouseById(hID).stream().iterator().next().toHouse();
                    jedis.set(String.format(HouseResource.HOUSE_CACHE_ENTRY_FORMAT, hID), mapper.writeValueAsString(h));
                }

                houses.add(h);
            }

            return houses;
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    private UserDAO getUser(CosmosPagedIterable<UserDAO> resGet ){
        Iterator<UserDAO> it = resGet.stream().iterator();
        return it.hasNext() ? it.next() : null;
    }




}
