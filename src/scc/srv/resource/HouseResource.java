package scc.srv.resource;

import com.azure.cosmos.CosmosException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import redis.clients.jedis.Jedis;
import scc.cache.RedisCache;
import scc.data.House;
import scc.data.HouseDAO;
import scc.db.CosmosDBLayer;
import scc.utils.Helpers;

import java.util.List;

@Path("/houses")
public class HouseResource {

    protected final static String HOUSE_ID = "houseId";

    private final String LOCATION = "location";

    protected static final String START_DATE = "startDate";

    protected static final String END_DATE = "endDate";

    protected static final String HOUSE_CACHE_ENTRY_FORMAT = "house:%s";

    private final String HOUSES_BY_LOCATION_CACHE_ENTRY_FORMAT = "location:%s:houses";

    private final String HOUSES_BY_LOCATION_AND_PERIOD_CACHE_ENTRY_FORMAT = "location:%s:startDate:%s:endDate:%s:houses";

    private final String DISCOUNTED_PERIODS_CACHE_ENTRY_FORMAT = "startDate:%s:endDate:%s:discounted_periods";
    private final CosmosDBLayer db;

    public HouseResource(){
        db = CosmosDBLayer.getInstance();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createHouse(@CookieParam("scc:session") Cookie session, House house){

        if(Helpers.checkCookieUser(session, house.getOwnerId()) == null && Helpers.AUTH_ON)
            return Response.status(Status.UNAUTHORIZED).build();

        HouseDAO hDAO = new HouseDAO(house);
        db.createHouse(hDAO);

        return Response.ok(hDAO).build();
    }

    @DELETE
    @Path("/{"+ HOUSE_ID + "}")
    public Response deleteHouse(@CookieParam("scc:session") Cookie session, @PathParam(HOUSE_ID) String id){
        try(Jedis jedis = RedisCache.getCachePool().getResource()) {

            HouseDAO hDAO = existentHouse(jedis, id, db);

            if(Helpers.checkCookieUser(session, hDAO.getOwnerId()) == null && Helpers.AUTH_ON)
                return Response.status(Status.UNAUTHORIZED).build();

            jedis.del(String.format(HOUSE_CACHE_ENTRY_FORMAT, id));
            db.delHouseById(id);

            return Response.ok().build();

        } catch (CosmosException e){
            if (e.getStatusCode() == 404)
                return Response.status(Status.NOT_FOUND).build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.serverError().build();
    }

    @PUT
    @Path("/{"+ HOUSE_ID + "}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateHouse(@CookieParam("scc:session") Cookie session, @PathParam(HOUSE_ID) String id, House house){

            if(Helpers.checkCookieUser(session, house.getOwnerId()) == null && Helpers.AUTH_ON)
                return Response.status(Status.UNAUTHORIZED).build();

            HouseDAO hDAO = new HouseDAO(house);
            hDAO.setId(id);

            try{
                db.updateHouse(hDAO);
            }
             catch (CosmosException e) {
                if (e.getStatusCode() == 404)
                    return Response.status(Status.NOT_FOUND).build();
            }

            return Response.ok().build();
    }

    @GET
    @Path("/{"+ HOUSE_ID + "}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHouse(@PathParam(HOUSE_ID) String id){
        try(Jedis jedis = RedisCache.getCachePool().getResource()) {
            HouseDAO hDAO = existentHouse(jedis, id, db);

            String idInCache = String.format(HOUSE_CACHE_ENTRY_FORMAT, id);
            jedis.set(idInCache, new ObjectMapper().writeValueAsString(hDAO));
            jedis.expire(idInCache, 120);

            return Response.ok(hDAO.toHouse()).build();
        } catch (CosmosException e) {
            if (e.getStatusCode() == 404)
                return Response.status(Status.NOT_FOUND).build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.serverError().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response availableHousesByFilter(@QueryParam(LOCATION) String location,
                                            @QueryParam(START_DATE) String startDate,
                                            @QueryParam(END_DATE) String endDate) {
        try(Jedis jedis = RedisCache.getCachePool().getResource()) {

            ObjectMapper mapper = new ObjectMapper();
            String res;
            List<HouseDAO> availableHouses = null;
            String idInCache = null;

            if (location != null){
                if (startDate != null && endDate != null) {

                    idInCache = String.format(HOUSES_BY_LOCATION_AND_PERIOD_CACHE_ENTRY_FORMAT, location, startDate, endDate);
                    res = jedis.get(idInCache);
                    if(res != null)
                        return Response.ok(mapper.readValue(res, List.class)).build();

                    availableHouses = db.getHousesByPeriodAndLocation(startDate, endDate, location);

                }else {

                    idInCache = String.format(HOUSES_BY_LOCATION_CACHE_ENTRY_FORMAT, location);
                    res = jedis.get(idInCache);
                    if(res != null)
                        return Response.ok(mapper.readValue(res, List.class)).build();
                    availableHouses = db.getHousesByLocation(location).stream().toList();
                }
            }

            if (availableHouses != null){
                jedis.set(idInCache, mapper.writeValueAsString(availableHouses));
                jedis.expire(idInCache, 30);
                return Response.ok(availableHouses).build();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.status(Status.BAD_REQUEST).build();
    }

    /*@GET
    @Path("/discounted")
    @Produces(MediaType.APPLICATION_JSON)
    public Response discountedPeriods(@QueryParam(START_DATE) String startDate, @QueryParam(END_DATE) String endDate){
        try(Jedis jedis = RedisCache.getCachePool().getResource()){
            String start = Helpers.toISO8601String(startDate);
            String end = Helpers.toISO8601String(endDate);

            String periodsInCache = String.format(DISCOUNTED_PERIODS_CACHE_ENTRY_FORMAT, start, end);
            String res = jedis.get(periodsInCache);
            ObjectMapper mapper = new ObjectMapper();

            if(res != null)
                return Response.ok(mapper.readValue(res, List.class)).build();

            List<PeriodDAO> periods = db.discountedPeriods(start, end).stream().toList();

            jedis.set(periodsInCache, mapper.writeValueAsString(periods));
            jedis.expire(periodsInCache, 60);
            return Response.ok(periods).build();
        }catch (Exception e){
            e.printStackTrace();
        }

        return Response.serverError().build();
    }
*/
    protected static HouseDAO existentHouse(Jedis jedis, String id, CosmosDBLayer db) throws JsonProcessingException {
        String houseIdInCache = String.format(HOUSE_CACHE_ENTRY_FORMAT, id);
        String res = jedis.get(houseIdInCache);

        return res != null ? new ObjectMapper().readValue(res, HouseDAO.class) : db.getHouseById(id).getItem();
    }
}
