package scc.srv.resource;

import scc.data.House;
import scc.data.HouseDAO;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

@Path("/house")
public class HouseResource {

    private final String ID = "id";

    private final Map<String, House> houses;

    public HouseResource(){
        houses = new HashMap<>();
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String createHouse(House house){
        return null;
    }

    @DELETE
    @Path("/{"+ ID + "})")
    @Produces(MediaType.APPLICATION_JSON)
    public House deleteHouse(@PathParam(ID) String id){
        return null;
    }

    @PUT
    @Path("/{"+ ID + "})")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public House updateHouse(@PathParam(ID) String id, House house){
        return null;
    }

    @GET
    @Path("/{"+ ID + "})")
    @Produces(MediaType.APPLICATION_JSON)
    public House getHouse(@PathParam(ID) String id){
        return null;
    }
}
