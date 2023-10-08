package scc.srv.resource;

import com.azure.core.exception.ResourceExistsException;
import com.azure.core.http.HttpResponse;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.implementation.ConflictException;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.util.CosmosPagedIterable;
import scc.data.User;
import scc.data.UserDAO;
import scc.db.CosmosDBLayer;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Path("/user")
public class UserResource {

    private final String ID = "id";
    private final String PWD = "pwd";
    private Map<String, User> users;

    public UserResource(){
        users = new HashMap<>();
    }

    @Path("/")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String createUser(User user){
        try{
            CosmosDBLayer db = CosmosDBLayer.getInstance();
            CosmosPagedIterable<UserDAO> resGet = db.getUserById(user.getId());
            UserDAO u = getUser(resGet);

            if(u != null)
                throw new Exception("User already exists.");

            db.createUser(new UserDAO(user));

            return user.getName();
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    @DELETE
    @Path("/{"+ ID + "}")
    @Produces(MediaType.APPLICATION_JSON)
    public User deleteUser(@PathParam(ID) String id, @QueryParam(PWD) String pwd){
            try{
                CosmosDBLayer db = CosmosDBLayer.getInstance();
                CosmosPagedIterable<UserDAO> resGet = db.getUserById(id);
                UserDAO u = getUser(resGet);

                if(u == null)
                    throw new Exception("User does not exists.");

                if(!u.getPwd().equals(pwd))
                    throw new Exception("Password does not match.");

                CosmosItemResponse<Object> res = db.delUserById(id);

                return (User) res.getItem();
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
        try{
            CosmosDBLayer db = CosmosDBLayer.getInstance();
            CosmosPagedIterable<UserDAO> resGet = db.getUserById(id);
            UserDAO u = getUser(resGet);

            if(u == null)
                throw new Exception("User does not exists.");

            if(!u.getPwd().equals(pwd))
                throw new Exception("Password does not match.");

            CosmosItemResponse<UserDAO> res = db.updateUser(u);

            return new User(res.getItem());
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @GET
    @Path("/{"+ ID + "}")
    @Produces(MediaType.APPLICATION_JSON)
    public User getUserById(@PathParam(ID) String id, @QueryParam(PWD) String pwd){
        try{
            CosmosDBLayer db = CosmosDBLayer.getInstance();
            CosmosPagedIterable<UserDAO> resGet = db.getUserById(id);
            UserDAO u = getUser(resGet);

            if(u == null)
                throw new Exception("User does not exists.");

            if(!u.getPwd().equals(pwd))
                throw new Exception("Password does not match.");

            return new User(u);
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
