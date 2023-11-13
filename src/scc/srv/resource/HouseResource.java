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
import scc.db.CosmosDBLayer;

import java.util.List;
import java.util.stream.Collectors;

@Path("/houses")
public class HouseResource {

    protected final static String HOUSE_ID = "houseId";

    private final String LOCATION = "location";

    private final String START_DATE = "startDate";

    private final String END_DATE = "endDate";

    protected static final String HOUSE_CACHE_ENTRY_FORMAT = "house:%s";

    private final String HOUSES_BY_LOCATION_CACHE_ENTRY_FORMAT = "location:%s:houses";

    private final String HOUSES_BY_LOCATION_AND_PERIOD_CACHE_ENTRY_FORMAT = "location:%s:startDate:%s:endDate:%s:houses";

    private final CosmosDBLayer db;

    public HouseResource(){
        db = CosmosDBLayer.getInstance();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createHouse(House house){
        try(Jedis jedis = RedisCache.getCachePool().getResource()) {
            HouseDAO hDAO = new HouseDAO(house);

            String id = hDAO.getId();
            String idInCache = String.format(HOUSE_CACHE_ENTRY_FORMAT, id);

            db.createHouse(hDAO);

            jedis.set(idInCache, new ObjectMapper().writeValueAsString(hDAO));

            return Response.ok(id).build();

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

            HouseDAO hDAO = new HouseDAO(house);
            hDAO.setId(id);
            hDAO = db.updateHouse(hDAO).getItem();

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


                    /*idInCache = String.format(HOUSES_BY_LOCATION_AND_PERIOD_CACHE_ENTRY_FORMAT, location, startDate, endDate);
                    res = jedis.get(idInCache);
                    if(res != null)
                        return Response.ok(mapper.readValue(res, List.class)).build();*/

                    return db.getHousesByPeriodAndLocation(startDate, endDate, location);

                }else {
                    /*idInCache = String.format(HOUSES_BY_LOCATION_CACHE_ENTRY_FORMAT, location);
                    res = jedis.get(idInCache);
                    if(res != null)
                        return Response.ok(mapper.readValue(res, List.class)).build();*/
                    availableHouses = db.getHousesByLocation(location).stream().collect(Collectors.toList());
                }
            }

            if (availableHouses != null){
               // jedis.set(idInCache, mapper.writeValueAsString(availableHouses));
                return Response.ok(availableHouses).build();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.status(Response.Status.BAD_REQUEST).build();
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
