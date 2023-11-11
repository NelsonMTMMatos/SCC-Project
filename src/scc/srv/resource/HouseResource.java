package scc.srv.resource;

import com.azure.cosmos.CosmosException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import redis.clients.jedis.Jedis;
import scc.cache.RedisCache;
import scc.data.House;
import scc.data.HouseDAO;
import scc.data.Question;
import scc.data.QuestionDAO;
import scc.db.CosmosDBLayer;

import java.util.List;
import java.util.stream.Collectors;

@Path("/houses")
public class HouseResource {

    private final String HOUSE_ID = "houseId";

    private final String QUESTION_ID = "questionId";

    private final String LOCATION = "location";

    private final String START_DATE = "startDate";

    private final String END_DATE = "endDate";

    public static final String HOUSE_CACHE_ENTRY_FORMAT = "house:%s";

    private final String QUESTION_CACHE_ENTRY_FORMAT = "question:%s";

    private final CosmosDBLayer db;

    public HouseResource(){
        db = CosmosDBLayer.getInstance();
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createHouse(House house){
        try(Jedis jedis = RedisCache.getCachePool().getResource()) {

            String id = house.getId();
            String idInCache = String.format(HOUSE_CACHE_ENTRY_FORMAT, id);

            HouseDAO hDAO = new HouseDAO(house);
            db.createHouse(hDAO);

            jedis.set(idInCache, new ObjectMapper().writeValueAsString(hDAO));

            return Response.ok(id).build();
        } catch (CosmosException e) {
            if (e.getStatusCode() == 409) {
                return Response.status(Response.Status.CONFLICT).build();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.serverError().build();
    }

    @DELETE
    @Path("/{"+ HOUSE_ID + "}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteHouse(@PathParam(HOUSE_ID) String id){
        try(Jedis jedis = RedisCache.getCachePool().getResource()) {

            jedis.del(String.format(HOUSE_CACHE_ENTRY_FORMAT, id));
            db.delHouseById(id).getItem();

            return Response.ok().build();
        } catch (CosmosException e){
            if (e.getStatusCode() == 404) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.serverError().build();
    }

    @PUT
    @Path("/{"+ HOUSE_ID + "}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateHouse(@PathParam(HOUSE_ID) String id, House house){
        try(Jedis jedis = RedisCache.getCachePool().getResource()) {

            HouseDAO hDAO = db.updateHouse(new HouseDAO(house)).getItem();

            String idInCache = String.format(HOUSE_CACHE_ENTRY_FORMAT, id);
            jedis.set(idInCache, new ObjectMapper().writeValueAsString(hDAO));

            return Response.ok(hDAO.toHouse()).build();

        } catch (CosmosException e) {
            if (e.getStatusCode() == 404) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.serverError().build();
    }

    @GET
    @Path("/{"+ HOUSE_ID + "}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHouse(@PathParam(HOUSE_ID) String id){
        try(Jedis jedis = RedisCache.getCachePool().getResource()) {
            HouseDAO hDAO = existentHouse(jedis, id, db);

            String idInCache = String.format(HOUSE_CACHE_ENTRY_FORMAT, id);
            jedis.set(idInCache, new ObjectMapper().writeValueAsString(hDAO));

            return Response.ok(hDAO.toHouse()).build();

        } catch (CosmosException e) {
            if (e.getStatusCode() == 404) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.serverError().build();
    }


    @POST
    @Path("/{"+ HOUSE_ID + "}/question")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createQuestion(@PathParam(HOUSE_ID) String houseId, Question question){
        try(Jedis jedis = RedisCache.getCachePool().getResource()){

            existentHouse(jedis, houseId, db);

            QuestionDAO newQuestion = new QuestionDAO(question);

            db.createQuestion(newQuestion);

            String questionIdInCache = String.format(QUESTION_CACHE_ENTRY_FORMAT, question.getHouseId());
            jedis.set(questionIdInCache, new ObjectMapper().writeValueAsString(newQuestion));

            return Response.ok(newQuestion.getId()).build();
        } catch (CosmosException e) {
            if (e.getStatusCode() == 409) {
                return Response.status(Response.Status.CONFLICT).build();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

        return Response.serverError().build();
    }

    @PUT
    @Path("/{"+ HOUSE_ID + "}/question/{" + QUESTION_ID + "}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response replyToQuestion(@PathParam(HOUSE_ID) String houseId, @PathParam(QUESTION_ID) String questionId, String reply){
        try(Jedis jedis = RedisCache.getCachePool().getResource()){

            existentHouse(jedis, houseId, db);

            String questionIdInCache = String.format(QUESTION_CACHE_ENTRY_FORMAT, questionId);
            String qRes = jedis.get(questionIdInCache);

            ObjectMapper mapper = new ObjectMapper();

            QuestionDAO qDao;
            if (qRes != null)
                qDao = mapper.readValue(qRes, QuestionDAO.class);
            else
                qDao = db.getQuestionById(questionId).getItem();

            // check if question belongs to house

            qDao.setReplyContent(reply);
            db.replyToQuestion(qDao);
            jedis.set(questionIdInCache, mapper.writeValueAsString(qDao));

            return Response.ok().build();
        }catch (CosmosException e) {
            if (e.getStatusCode() == 404) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.serverError().build();
    }

    @GET
    @Path("/{"+ HOUSE_ID + "}/questions")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listQuestions(@PathParam(HOUSE_ID) String houseId){
        try(Jedis jedis = RedisCache.getCachePool().getResource()){
            existentHouse(jedis, houseId, db);

            //Check if query is in cache

            List<QuestionDAO> questions = db.getHouseQuestions(houseId).stream().collect(Collectors.toList());

            //Put query result in cache

            return Response.ok(questions).build();
        }catch (Exception e){
            e.printStackTrace();
        }

        return Response.serverError().build();
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response availableHousesByFilter(@QueryParam(LOCATION) String location,
                                            @QueryParam(START_DATE) String startDate,
                                            @QueryParam(END_DATE) String endDate){
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }

    @GET
    @Path("/discounted")
    @Produces(MediaType.APPLICATION_JSON)
    public Response discountedRentals(@QueryParam(START_DATE) String startDate, @QueryParam(END_DATE) String endDate){
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }

    protected static HouseDAO existentHouse(Jedis jedis, String id, CosmosDBLayer db) throws JsonProcessingException {
        String houseIdInCache = String.format(HOUSE_CACHE_ENTRY_FORMAT, id);
        String res = jedis.get(houseIdInCache);

        return res != null ? new ObjectMapper().readValue(res, HouseDAO.class) : db.getHouseById(id).getItem();
    }


}
