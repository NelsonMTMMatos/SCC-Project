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

import java.util.List;
import java.util.stream.Collectors;

import static scc.srv.resource.HouseResource.HOUSE_ID;
import static scc.srv.resource.HouseResource.existentHouse;

@Path("/houses/{" + HOUSE_ID + "}/periods")
public class PeriodResource {

    private final String PERIOD_CACHE_ENTRY_FORMAT = "period:%s";

    private final String HOUSE_PERIODS_CACHE_ENTRY_FORMAT = "house:%s:periods";

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
            newPeriod.setHouseId(houseId);

            db.createPeriod(newPeriod);

            String periodIdInCache = String.format(PERIOD_CACHE_ENTRY_FORMAT, newPeriod.getId());
            jedis.set(periodIdInCache, new ObjectMapper().writeValueAsString(newPeriod));

            return Response.ok(newPeriod.getId()).build();

        } catch (CosmosException e) {
            if (e.getStatusCode() == 404)
                return Response.status(Response.Status.NOT_FOUND).build();
        }catch (Exception e) {
            return Response.status(401, "Period intersects with others of same price").build();
        }

        return Response.serverError().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAvailability(@PathParam(HOUSE_ID) String houseId){
        try(Jedis jedis = RedisCache.getCachePool().getResource()){
            existentHouse(jedis, houseId, db);

            String periodsInCache = String.format(HOUSE_PERIODS_CACHE_ENTRY_FORMAT, houseId);
            String res = jedis.get(periodsInCache);
            ObjectMapper mapper = new ObjectMapper();

            if(res != null)
                return Response.ok(mapper.readValue(res, List.class)).build();

            List<PeriodDAO> periods = db.getHousePeriods(houseId).stream().toList();

            jedis.set(periodsInCache, mapper.writeValueAsString(periods));
            jedis.expire(periodsInCache, 30);

            return Response.ok(periods).build();

        } catch (CosmosException e) {
            if (e.getStatusCode() == 404)
                return Response.status(Response.Status.NOT_FOUND).build();
        }catch (Exception e){
            e.printStackTrace();
        }

        return Response.serverError().build();
    }


}