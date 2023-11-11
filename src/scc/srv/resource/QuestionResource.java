package scc.srv.resource;

import com.azure.cosmos.CosmosException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import redis.clients.jedis.Jedis;
import scc.cache.RedisCache;
import scc.data.Question;
import scc.data.QuestionDAO;
import scc.db.CosmosDBLayer;

import java.util.List;
import java.util.stream.Collectors;

import static scc.srv.resource.HouseResource.existentHouse;
import static scc.srv.resource.HouseResource.HOUSE_ID;

@Path("/houses/{houseId}/questions")
public class QuestionResource {

    private final String QUESTION_ID = "questionId";

    private final String QUESTION_CACHE_ENTRY_FORMAT = "question:%s";

    private final CosmosDBLayer db;

    public QuestionResource(){
        db = CosmosDBLayer.getInstance();
    }

    @POST
    @Path("/")
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
    @Path("/{" + QUESTION_ID + "}")
    @Consumes(MediaType.TEXT_PLAIN)
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
    @Path("/")
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
}
