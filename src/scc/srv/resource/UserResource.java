package scc.srv.resource;

import com.azure.core.exception.ResourceExistsException;
import com.azure.core.http.HttpResponse;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.implementation.ConflictException;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.Jedis;
import scc.cache.RedisCache;
import scc.data.House;
import scc.data.User;
import scc.data.UserDAO;
import scc.db.CosmosDBLayer;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

@Path("/user")
public class UserResource {

    private final String ID = "id";
    private final String PWD = "pwd";

    public UserResource(){}

    @Path("/")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String createUser(User user){
        try(Jedis jedis = RedisCache.getCachePool().getResource()){
            CosmosDBLayer db = CosmosDBLayer.getInstance();
            CosmosPagedIterable<UserDAO> resGet = db.getUserById(user.getId());
            UserDAO uDao = getUser(resGet);

            if(uDao != null)
                throw new Exception("User already exists.");

            db.createUser(new UserDAO(user));

            jedis.set("user:" + user.getId(), new ObjectMapper().writeValueAsString(user));

            return user.getId();
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
                ObjectMapper mapper = new ObjectMapper();

                String res = jedis.get("user:" + id);

                User u = mapper.readValue(res, User.class);

                if(u != null) if(u.getPwd().equals(pwd)){
                    jedis.del("user:" + id);
                    return u;
                }


                CosmosDBLayer db = CosmosDBLayer.getInstance();
                CosmosPagedIterable<UserDAO> resGet = db.getUserById(id);
                UserDAO uDao = getUser(resGet);

                if(uDao == null)
                    throw new Exception("User does not exists.");

                if(!uDao.getPwd().equals(pwd))
                    throw new Exception("Password does not match.");

                CosmosItemResponse<Object> resDb = db.delUserById(id);

                return (User) resDb.getItem();
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
            ObjectMapper mapper = new ObjectMapper();

            String res = jedis.get("user:" + id);

            User u = mapper.readValue(res, User.class);

            if(u != null) if(u.getPwd().equals(pwd)) return u;

            CosmosDBLayer db = CosmosDBLayer.getInstance();
            CosmosPagedIterable<UserDAO> resGet = db.getUserById(id);
            UserDAO uDao = getUser(resGet);

            if(uDao == null)
                throw new Exception("User does not exists.");

            if(!uDao.getPwd().equals(pwd))
                throw new Exception("Password does not match.");

            CosmosItemResponse<UserDAO> resDb = db.updateUser(uDao);
            jedis.del("user:" + id);
            u = resDb.getItem().toUser();
            jedis.set("user:" + id, new ObjectMapper().writeValueAsString(u));
            return u;
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

            String res = jedis.get("user:" + id);

            User u = mapper.readValue(res, User.class);

            if(u != null) if(u.getPwd().equals(pwd)) return u;

            CosmosDBLayer db = CosmosDBLayer.getInstance();
            CosmosPagedIterable<UserDAO> resGet = db.getUserById(id);
            UserDAO uDao = getUser(resGet);

            if(uDao == null)
                throw new Exception("User does not exists.");

            if(!uDao.getPwd().equals(pwd))
                throw new Exception("Password does not match.");

            u = uDao.toUser();
            jedis.set("user:" + id, mapper.writeValueAsString(u));
            return u;
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
        try{
            //TODO: Verify if there are houses in cache, if not just go to database
            CosmosDBLayer db = CosmosDBLayer.getInstance();
            CosmosPagedIterable<UserDAO> resGet = db.getUserById(id);
            UserDAO u = getUser(resGet);

            if(u == null)
                throw new Exception("User does not exists.");

            Set<House> houses = new HashSet<>();
            for(String houseID: u.getHouseIds())
                houses.add(db.getHouseById(houseID).stream().iterator().next().toHouse());

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
