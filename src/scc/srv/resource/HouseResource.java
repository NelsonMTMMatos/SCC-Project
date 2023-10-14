package scc.srv.resource;

import com.azure.cosmos.util.CosmosPagedIterable;
import scc.data.*;
import scc.db.CosmosDBLayer;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.time.Period;
import java.util.*;

@Path("/house")
public class HouseResource {

    private final String HOUSE_ID = "houseId";
    private final String RENTAL_ID = "rentalId";

    private final String QUESTION_ID = "questionId";

    private final String LOCATION = "location";

    private final String PERIOD = "period";

    private final Map<String, House> houses;

    public HouseResource(){
        houses = new HashMap<>();
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String createHouse(House house){
        try {

            CosmosDBLayer db = CosmosDBLayer.getInstance();
            CosmosPagedIterable<HouseDAO> resGet = db.getHouseById(house.getId());
            HouseDAO h = getHouse(resGet);
            if (h != null) {
                throw new Exception("Rental already exists.");
            }
            db.createHouse(new HouseDAO(house));
            return house.getId();

        } catch (Exception e) {
            e.printStackTrace();
        }
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
            CosmosPagedIterable<HouseDAO> resH = db.getHouseById(houseId);
            HouseDAO h = getHouse(resH);
            if (h == null) {
                throw new Exception("House does not exist.");
            }
            CosmosPagedIterable<RentalDAO> resR = db.getRentalById(rental.getId());
            RentalDAO r = getRental(resR);
            if (r != null) {
                throw new Exception("Rental already exists.");
            }
            db.createRental(new RentalDAO(rental));
            return rental.getId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @PUT
    @Path("/{"+ HOUSE_ID + "}/rental/{" + RENTAL_ID + "}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Rental updateRental(@PathParam(HOUSE_ID) String houseId, @PathParam(RENTAL_ID) String rentalId, Rental rental) {
        try {
            CosmosDBLayer db = CosmosDBLayer.getInstance();
            CosmosPagedIterable<HouseDAO> resH = db.getHouseById(houseId);
            HouseDAO h = getHouse(resH);
            if (h == null) {
                throw new Exception("House does not exist.");
            }
            CosmosPagedIterable<RentalDAO> resR = db.getRentalById(rentalId);
            RentalDAO r = getRental(resR);
            if (r == null) {
                throw new Exception("Rental doesn't exist.");
            }
            db.updateRental(new RentalDAO(rental));
            return rental;
        } catch (Exception e) {
            System.err.println(e.toString());
        }
        return null;
    }

    @GET
    @Path("/{"+ HOUSE_ID + "}/rental/{" + RENTAL_ID + "}")
    @Produces(MediaType.APPLICATION_JSON)
    public Rental getRental(@PathParam(HOUSE_ID) String houseId, @PathParam(RENTAL_ID) String rentalId) {
        try {
            CosmosDBLayer db = CosmosDBLayer.getInstance();
            CosmosPagedIterable<HouseDAO> resH = db.getHouseById(houseId);
            HouseDAO h = getHouse(resH);
            if (h == null) {
                throw new Exception("House does not exist.");
            }
            CosmosPagedIterable<RentalDAO> resR = db.getRentalById(rentalId);
            RentalDAO r = getRental(resR);
            if (r == null) {
                throw new Exception("Rental doesn't exist.");
            }
            return r.toRental();
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
    public String replyToQuestion(@PathParam(HOUSE_ID) String houseId, @PathParam(QUESTION_ID) String questionId, String reply){
        return null;
    }

    @GET
    @Path("/{"+ HOUSE_ID + "}/question")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<String> listQuestions(@PathParam(HOUSE_ID) String houseId){
        return null;
    }


    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<House> availableHousesByLocation(@QueryParam(LOCATION) String location){
        return null;
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<House> availableHousesByPeriodAndLocation(@QueryParam(LOCATION) String location, @QueryParam(PERIOD) Period period){
        return null;
    }

    @GET
    @Path("/{"+ HOUSE_ID + "}/rental/{" + RENTAL_ID + "}/discounted")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<Rental> discountedRentals(@PathParam(HOUSE_ID) String houseId, @QueryParam(PERIOD) Period period){
        return null;
    }

    private HouseDAO getHouse(CosmosPagedIterable<HouseDAO> resGet ){
        Iterator<HouseDAO> it = resGet.stream().iterator();
        return it.hasNext() ? it.next() : null;
    }

    private RentalDAO getRental(CosmosPagedIterable<RentalDAO> resGet ){
        Iterator<RentalDAO> it = resGet.stream().iterator();
        return it.hasNext() ? it.next() : null;
    }

}
