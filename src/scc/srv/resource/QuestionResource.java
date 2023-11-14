package scc.srv.resource;

import com.azure.cosmos.CosmosException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import redis.clients.jedis.Jedis;
import scc.cache.RedisCache;
import scc.data.HouseDAO;
import scc.data.Question;
import scc.data.QuestionDAO;
import scc.db.CosmosDBLayer;
import scc.utils.Helpers;

import java.util.List;

import static scc.srv.resource.HouseResource.HOUSE_ID;
import static scc.srv.resource.HouseResource.existentHouse;

@Path("/houses/{" + HOUSE_ID + "}/questions")
public class QuestionResource {

    private final String QUESTION_ID = "questionId";

    private final String HOUSE_QUESTIONS_CACHE_ENTRY_FORMAT = "house:%s:questions";

    private final CosmosDBLayer db;

    public QuestionResource(){
        db = CosmosDBLayer.getInstance();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createQuestion(@CookieParam("scc:session") Cookie session, @PathParam(HOUSE_ID) String houseId, Question question){

        if(Helpers.checkCookieUser(session, question.getUserId()) == null && Helpers.AUTH_ON)
            return Response.status(Status.UNAUTHORIZED).build();

        try(Jedis jedis = RedisCache.getCachePool().getResource()){

            existentHouse(jedis, houseId, db);

            QuestionDAO newQuestion = new QuestionDAO(question);
            newQuestion.setHouseId(houseId);

            newQuestion = db.createQuestion(newQuestion).getItem();

            return Response.ok(newQuestion.getId()).build();
        } catch (CosmosException e) {
            if (e.getStatusCode() == 404)
                return Response.status(Status.NOT_FOUND).build();
        }catch (Exception e) {
            e.printStackTrace();
        }

        return Response.serverError().build();
    }

    @PUT
    @Path("/{" + QUESTION_ID + "}")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response replyToQuestion(@CookieParam("scc:session") Cookie session, @PathParam(HOUSE_ID) String houseId, @PathParam(QUESTION_ID) String questionId, String reply){
        try(Jedis jedis = RedisCache.getCachePool().getResource()){
            HouseDAO hDAO = existentHouse(jedis, houseId, db);

            if(Helpers.checkCookieUser(session, hDAO.getOwnerId()) == null && Helpers.AUTH_ON)
                return Response.status(Status.UNAUTHORIZED).build();

            QuestionDAO qDao = db.getQuestionById(questionId).getItem();

            if(!houseId.equals(qDao.getHouseId()))
                return Response.status(Status.UNAUTHORIZED).build();

            qDao.setReplyContent(reply);
            db.replyToQuestion(qDao);

            return Response.ok().build();
        }catch (CosmosException e) {
            if (e.getStatusCode() == 404)
                return Response.status(Status.NOT_FOUND).build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.serverError().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listQuestions(@PathParam(HOUSE_ID) String houseId){
        try(Jedis jedis = RedisCache.getCachePool().getResource()){
            existentHouse(jedis, houseId, db);

            String questionsInCache = String.format(HOUSE_QUESTIONS_CACHE_ENTRY_FORMAT, houseId);
            String res = jedis.get(questionsInCache);
            ObjectMapper mapper = new ObjectMapper();

            if(res != null)
                return Response.ok(mapper.readValue(res, List.class)).build();

            List<QuestionDAO> questions = db.getHouseQuestions(houseId).stream().toList();

            jedis.set(questionsInCache, mapper.writeValueAsString(questions));
            jedis.expire(questionsInCache, 90);

            return Response.ok(questions).build();
        } catch (CosmosException e) {
            if (e.getStatusCode() == 404)
                return Response.status(Status.NOT_FOUND).build();
        }catch (Exception e){
            e.printStackTrace();
        }

        return Response.serverError().build();
    }
}
