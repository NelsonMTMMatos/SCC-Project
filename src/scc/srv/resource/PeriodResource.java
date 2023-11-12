package scc.srv.resource;

import com.azure.cosmos.CosmosException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import redis.clients.jedis.Jedis;
import scc.cache.RedisCache;
import scc.data.Period;
import scc.data.PeriodDAO;
import scc.db.CosmosDBLayer;

import static scc.srv.resource.HouseResource.HOUSE_ID;
import static scc.srv.resource.HouseResource.existentHouse;

@Path("/houses/{houseId}/periods")
public class PeriodResource {

    private final String PERIOD_ID = "periodId";

    private final String PERIOD_CACHE_ENTRY_FORMAT = "period:%s";

    private final CosmosDBLayer db;

    public PeriodResource(){
        db = CosmosDBLayer.getInstance();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addPeriod(@PathParam(HOUSE_ID) String houseId, Period period){
        try(Jedis jedis = RedisCache.getCachePool().getResource()){

            existentHouse(jedis, houseId, db);

            PeriodDAO newPeriod = new PeriodDAO(period);

            db.createPeriod(newPeriod);

            String periodIdInCache = String.format(PERIOD_CACHE_ENTRY_FORMAT, newPeriod.getId());
            jedis.set(periodIdInCache, new ObjectMapper().writeValueAsString(newPeriod));

            return Response.ok(newPeriod.getId()).build();
        } catch (CosmosException e) {
            if (e.getStatusCode() == 409)
                return Response.status(Response.Status.CONFLICT).build();
            if (e.getStatusCode() == 404)
                return Response.status(Response.Status.NOT_FOUND).build();
        }catch (Exception e) {
            return Response.status(401, "Period intersects with others of same price").build();
        }

        return Response.serverError().build();
    }

}