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
                throw new Exception("House already exists.");
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
        try {
            CosmosDBLayer db = CosmosDBLayer.getInstance();
            HouseDAO h = (HouseDAO) db.delHouseById(id).getItem();

            if (h == null) {
                throw new Exception("House didn't exist.");
            }

            return h.toHouse();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @PUT
    @Path("/{"+ HOUSE_ID + "}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public House updateHouse(@PathParam(HOUSE_ID) String id, House house){
        try {
            CosmosDBLayer db = CosmosDBLayer.getInstance();
            CosmosPagedIterable<HouseDAO> resGet = db.getHouseById(house.getId());
            HouseDAO h = getHouse(resGet);

            if (h == null) {
                throw new Exception("House didn't exist.");
            }

            return db.updateHouse(h).getItem().toHouse();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @GET
    @Path("/{"+ HOUSE_ID + "}")
    @Produces(MediaType.APPLICATION_JSON)
    public House getHouse(@PathParam(HOUSE_ID) String id){
        try {
            CosmosDBLayer db = CosmosDBLayer.getInstance();
            CosmosPagedIterable<HouseDAO> resGet = db.getHouseById(id);
            HouseDAO h = getHouse(resGet);

            if (h == null) {
                throw new Exception("House didn't exist.");
            }

            return h.toHouse();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    @POST
    @Path("/{"+ HOUSE_ID + "}/rental")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String createRental(@PathParam(HOUSE_ID) String houseId, Rental rental) {
        try(Jedis jedis = RedisCache.getCachePool().getResource()) {

            CosmosDBLayer db = CosmosDBLayer.getInstance();

            String houseIdInCache = String.format(HOUSE_CACHE_ENTRY_FORMAT, houseId);
            String hRes = jedis.get(houseIdInCache);

            if (hRes == null) {
                CosmosPagedIterable<HouseDAO> resGet = db.getHouseById(houseId);
                HouseDAO hDAO = getHouse(resGet);

                if (hDAO == null) {
                    throw new Exception("House does not exist.");
                }
            }

            String rentalId = rental.getId();
            String rentalIdInCache = String.format(RENTAL_CACHE_ENTRY_FORMAT, rentalId);

            if (jedis.get(rentalIdInCache) != null) {
                throw new Exception("Rental already exists.");
            }

            CosmosPagedIterable<RentalDAO> resR = db.getRentalById(rental.getId());
            RentalDAO rDAO = getRental(resR);

            if (rDAO != null) {
                throw new Exception("Rental already exists.");
            }

            rDAO = new RentalDAO(rental);
            db.createRental(rDAO);

            jedis.set(rentalIdInCache, new ObjectMapper().writeValueAsString(rDAO));

            return rentalId;

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
        try(Jedis jedis = RedisCache.getCachePool().getResource()) {

            CosmosDBLayer db = CosmosDBLayer.getInstance();

            String houseIdInCache = String.format(HOUSE_CACHE_ENTRY_FORMAT, houseId);
            String hRes = jedis.get(houseIdInCache);

            if (hRes == null) {
                CosmosPagedIterable<HouseDAO> resGet = db.getHouseById(houseId);
                HouseDAO hDAO = getHouse(resGet);

                if (hDAO == null) {
                    throw new Exception("House does not exist.");
                }
            }

            RentalDAO rDAO = new RentalDAO(rental);
            db.updateRental(rDAO);

            String rentalIdInCache = String.format(RENTAL_CACHE_ENTRY_FORMAT, rentalId);
            jedis.set(rentalIdInCache, new ObjectMapper().writeValueAsString(rDAO));

            return rental;
        } catch (CosmosException e) {
            if (e.getStatusCode() == 404) {
                logger.severe("Rental doesn't exist.");
            }
        } catch (Exception e) {
            System.err.println(e.toString());
        }
        return null;
    }

    @GET
    @Path("/{"+ HOUSE_ID + "}/rental/{" + RENTAL_ID + "}")
    @Produces(MediaType.APPLICATION_JSON)
    public Rental getRental(@PathParam(HOUSE_ID) String houseId, @PathParam(RENTAL_ID) String rentalId) {
        try(Jedis jedis = RedisCache.getCachePool().getResource()) {

            CosmosDBLayer db = CosmosDBLayer.getInstance();

            String houseIdInCache = String.format(HOUSE_CACHE_ENTRY_FORMAT, houseId);
            String hRes = jedis.get(houseIdInCache);

            if (hRes == null) {
                CosmosPagedIterable<HouseDAO> resGet = db.getHouseById(houseId);
                HouseDAO hDAO = getHouse(resGet);

                if (hDAO == null) {
                    throw new Exception("House does not exist.");
                }
            }

            String rentalIdInCache = String.format(RENTAL_CACHE_ENTRY_FORMAT, rentalId);
            ObjectMapper mapper = new ObjectMapper();
            RentalDAO rDAO;

            String res = jedis.get(rentalIdInCache);
            if (res != null) {
                rDAO = mapper.readValue(res, RentalDAO.class);
            } else {
                CosmosPagedIterable<RentalDAO> resR = db.getRentalById(rentalId);
                rDAO = getRental(resR);
                if (rDAO == null) {
                    throw new Exception("Rental does not exist.");
                }
            }

            jedis.set(rentalIdInCache, mapper.writeValueAsString(rDAO));
            return rDAO.toRental();
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
