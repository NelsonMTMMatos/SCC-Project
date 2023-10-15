package scc.srv.resource;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.simple.SimpleLogger;
import redis.clients.jedis.Jedis;
import scc.cache.RedisCache;
import scc.data.*;
import scc.db.CosmosDBLayer;
import scc.utils.Helpers;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.time.Period;
import java.util.*;
import java.util.logging.Logger;

@Path("/house")
public class HouseResource {

    private static final Logger logger = Logger.getLogger(HouseResource.class.getName());

    private final String HOUSE_ID = "houseId";
    private final String RENTAL_ID = "rentalId";

    private final String QUESTION_ID = "questionId";

    private final String LOCATION = "location";

    private final String PERIOD = "period";

    public static final String HOUSE_CACHE_ENTRY_FORMAT = "house:%s";

    private final String RENTAL_CACHE_ENTRY_FORMAT = "rental:%s";

    private final String QUESTION_CACHE_ENTRY_FORMAT = "question:%s";

    private final Map<String, House> houses;

    public HouseResource(){
        houses = new HashMap<>();
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String createHouse(House house){
        try(Jedis jedis = RedisCache.getCachePool().getResource()) {

            String id = house.getId();
            String idInCache = String.format(HOUSE_CACHE_ENTRY_FORMAT, id);

            if (jedis.get(idInCache) != null) {
                throw new Exception("House already exists.");
            }

            CosmosDBLayer db = CosmosDBLayer.getInstance();
            CosmosPagedIterable<HouseDAO> resGet = db.getHouseById(id);
            HouseDAO hDAO = Helpers.getItem(resGet);

            if (hDAO != null) {
                throw new Exception("House already exists.");
            }

            hDAO = new HouseDAO(house);
            db.createHouse(hDAO);

            jedis.set(idInCache, new ObjectMapper().writeValueAsString(hDAO));

            return id;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @DELETE
    @Path("/{"+ HOUSE_ID + "}")
    @Produces(MediaType.APPLICATION_JSON)
    public House deleteHouse(@PathParam(HOUSE_ID) String id){
        try(Jedis jedis = RedisCache.getCachePool().getResource()) {

            CosmosDBLayer db = CosmosDBLayer.getInstance();

            jedis.del(String.format(HOUSE_CACHE_ENTRY_FORMAT, id));
            HouseDAO hDAO = (HouseDAO) db.delHouseById(id).getItem();

            return hDAO.toHouse();
        } catch (CosmosException e){
            if (e.getStatusCode() == 404) {
                logger.severe("House doesn't exist.");
            }
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
        try(Jedis jedis = RedisCache.getCachePool().getResource()) {

            CosmosDBLayer db = CosmosDBLayer.getInstance();
            HouseDAO hDAO = db.updateHouse(new HouseDAO(house)).getItem();

            String idInCache = String.format(HOUSE_CACHE_ENTRY_FORMAT, id);
            jedis.set(idInCache, new ObjectMapper().writeValueAsString(hDAO));

            return hDAO.toHouse();
        } catch (CosmosException e) {
            if (e.getStatusCode() == 404) {
                logger.severe("House doesn't exist.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @GET
    @Path("/{"+ HOUSE_ID + "}")
    @Produces(MediaType.APPLICATION_JSON)
    public House getHouse(@PathParam(HOUSE_ID) String id){
        try(Jedis jedis = RedisCache.getCachePool().getResource()) {

            String idInCache = String.format(HOUSE_CACHE_ENTRY_FORMAT, id);
            String res = jedis.get(idInCache);
            HouseDAO hDAO;
            ObjectMapper mapper = new ObjectMapper();

            if (res != null) {
                hDAO = mapper.readValue(res, HouseDAO.class);
            }else{
                CosmosDBLayer db = CosmosDBLayer.getInstance();
                CosmosPagedIterable<HouseDAO> resGet = db.getHouseById(id);
                hDAO = Helpers.getItem(resGet);

                if (hDAO == null) {
                    throw new Exception("House didn't exist.");
                }
            }

            jedis.set(idInCache, mapper.writeValueAsString(hDAO));
            return hDAO.toHouse();

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
                HouseDAO hDAO = Helpers.getItem(resGet);

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
            RentalDAO rDAO = Helpers.getItem(resR);

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
                HouseDAO hDAO = Helpers.getItem(resGet);

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
            e.printStackTrace();
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
                HouseDAO hDAO = Helpers.getItem(resGet);

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
                rDAO = Helpers.getItem(resR);
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
        try(Jedis jedis = RedisCache.getCachePool().getResource()){
            CosmosDBLayer db = CosmosDBLayer.getInstance();

            String houseIdInCache = String.format(HOUSE_CACHE_ENTRY_FORMAT, houseId);
            String hRes = jedis.get(houseIdInCache);

            if (hRes == null) {
                CosmosPagedIterable<HouseDAO> resGet = db.getHouseById(houseId);
                HouseDAO hDAO = Helpers.getItem(resGet);

                if (hDAO == null) {
                    throw new Exception("House does not exist.");
                }
            }

            String questionIdInCache = String.format(QUESTION_CACHE_ENTRY_FORMAT, question.getHouseId());
            String qRes = jedis.get(questionIdInCache);

            if(qRes != null) throw new Exception("Question already exists.");

            QuestionDAO newQuestion = new QuestionDAO(question);

            db.createQuestion(newQuestion);
            jedis.set(questionIdInCache, new ObjectMapper().writeValueAsString(newQuestion));

            return newQuestion.getId();
        }catch (Exception e) {
            System.err.println(e.toString());
        }

        return null;
    }

    @PUT
    @Path("/{"+ HOUSE_ID + "}/question/{" + QUESTION_ID + "}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void replyToQuestion(@PathParam(HOUSE_ID) String houseId, @PathParam(QUESTION_ID) String questionId, String reply){
        try(Jedis jedis = RedisCache.getCachePool().getResource()){
            CosmosDBLayer db = CosmosDBLayer.getInstance();

            String houseIdInCache = String.format(HOUSE_CACHE_ENTRY_FORMAT, houseId);
            String hRes = jedis.get(houseIdInCache);

            if (hRes == null) {
                CosmosPagedIterable<HouseDAO> resGet = db.getHouseById(houseId);
                HouseDAO hDAO = Helpers.getItem(resGet);

                if (hDAO == null) {
                    throw new Exception("House does not exist.");
                }
            }

            String questionIdInCache = String.format(QUESTION_CACHE_ENTRY_FORMAT, questionId);
            String qRes = jedis.get(questionIdInCache);

            QuestionDAO qDao = new QuestionDAO();
            if(qRes == null){
                CosmosPagedIterable<QuestionDAO> resGet = db.getQuestionById(questionId);
                qDao = Helpers.getItem(resGet);

                if (qDao == null) {
                    throw new Exception("Question does not exist.");
                }
            }

            qDao.setReplyContent(reply);
            db.replyToQuestion(qDao);
            jedis.set(questionIdInCache, new ObjectMapper().writeValueAsString(qDao));
        }catch (CosmosException e) {
            if (e.getStatusCode() == 404) {
                logger.severe("Question doesn't exist.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GET
    @Path("/{"+ HOUSE_ID + "}/question")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<String> listQuestions(@PathParam(HOUSE_ID) String houseId){
        try(Jedis jedis = RedisCache.getCachePool().getResource()){
            CosmosDBLayer db = CosmosDBLayer.getInstance();

            String houseIdInCache = String.format(HOUSE_CACHE_ENTRY_FORMAT, houseId);
            String hRes = jedis.get(houseIdInCache);

            HouseDAO hDAO = new HouseDAO();
            if (hRes == null) {
                CosmosPagedIterable<HouseDAO> resGet = db.getHouseById(houseId);
                hDAO = Helpers.getItem(resGet);

                if (hDAO == null) {
                    throw new Exception("House does not exist.");
                }
            }

            Set<String> questions = new HashSet<>();
            for (String questionID: hDAO.getQuestionIds())
                questions.add(db.getQuestionById(questionID).stream().iterator().next().getQuestionContent());

            return questions;
        }catch (Exception e){
            System.err.println(e.toString());
        }
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

}
