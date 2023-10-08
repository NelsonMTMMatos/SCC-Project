package scc.srv.resource;

import scc.data.House;
import scc.data.HouseDAO;
import scc.data.Rental;
import scc.db.CosmosDBLayer;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

@Path("/house")
public class HouseResource {

    private final String HOUSE_ID = "houseId";
    private final String RENTAL_ID = "rentalId";

    private final String QUESTION_ID = "questionId";

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
    @Path("/{"+ HOUSE_ID + "}")
    @Produces(MediaType.APPLICATION_JSON)
    public House deleteHouse(@PathParam(HOUSE_ID) String id){
        return null;
    }

    @PUT
    @Path("/{"+ HOUSE_ID + "}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public House updateHouse(@PathParam(HOUSE_ID) String id, House house){
        return null;
    }

    @GET
    @Path("/{"+ HOUSE_ID + "}")
    @Produces(MediaType.APPLICATION_JSON)
    public House getHouse(@PathParam(HOUSE_ID) String id){
        return null;
    }


    @POST
    @Path("/{"+ HOUSE_ID + "}/rental")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String createRental(@PathParam(HOUSE_ID) String houseId, Rental rental) {
        try {
            CosmosDBLayer db = CosmosDBLayer.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @PUT
    @Path("/{"+ HOUSE_ID + "}/rental/{" + RENTAL_ID + "}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String updateRental(@PathParam(HOUSE_ID) String houseId, @PathParam(RENTAL_ID) String rentalId, Rental rental) {
        try {
            CosmosDBLayer db = CosmosDBLayer.getInstance();
        } catch (Exception e) {
            System.err.println(e.toString());
        }
        return null;
    }

    @GET
    @Path("/{"+ HOUSE_ID + "}/rental/{" + RENTAL_ID + "}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getRental(@PathParam(HOUSE_ID) String houseId, @PathParam(RENTAL_ID) String rentalId) {
        try {
            CosmosDBLayer db = CosmosDBLayer.getInstance();
        } catch (Exception e) {
            System.err.println(e.toString());
        }
        return null;
    }


    @POST
    @Path("/{"+ HOUSE_ID + "}/question")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String createQuestion(@PathParam(HOUSE_ID) String houseId, Question question){
        return null;
    }

    @POST
    @Path("/{"+ HOUSE_ID + "}/question/{" + QUESTION_ID + "}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String replyToQuestion(@PathParam(HOUSE_ID) String houseId, @PathParam(QUESTION_ID) String questionId, String reply){
        return null;
    }

    @GET
    @Path("/{"+ HOUSE_ID + "}/question")
    @Produces(MediaType.APPLICATION_JSON)
    public String listQuestions(@PathParam(HOUSE_ID) String houseId){
        return null;
    }

}
