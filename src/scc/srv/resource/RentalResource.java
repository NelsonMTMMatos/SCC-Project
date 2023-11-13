package scc.srv.resource;

import com.azure.cosmos.CosmosException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import redis.clients.jedis.Jedis;
import scc.authentication.Session;
import scc.cache.RedisCache;
import scc.data.HouseDAO;
import scc.data.Rental;
import scc.data.RentalDAO;
import scc.db.CosmosDBLayer;
import scc.utils.Helpers;

import java.util.NoSuchElementException;

import static scc.srv.resource.HouseResource.HOUSE_ID;
import static scc.srv.resource.HouseResource.existentHouse;

@Path("/houses/{" + HOUSE_ID + "}/rentals")
public class RentalResource {
    private final String RENTAL_ID = "rentalId";
    private final String RENTAL_CACHE_ENTRY_FORMAT = "rental:%s";
    private final CosmosDBLayer db;

    public RentalResource() {
        db = CosmosDBLayer.getInstance();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createRental(@CookieParam("scc:session") Cookie session, @PathParam(HOUSE_ID) String houseId, Rental rental) {
        try(Jedis jedis = RedisCache.getCachePool().getResource()) {

            Session s = Helpers.checkCookieUser(session, rental.getUserId());

            if(s == null)
                return Response.status(Response.Status.UNAUTHORIZED).build();

            HouseDAO hDAO = existentHouse(jedis, houseId, db);

            RentalDAO rDAO = new RentalDAO(rental);
            rDAO.setHouseId(houseId);
            rDAO.setPrice(hDAO.getPrice());

            rDAO = db.createRental(rDAO).getItem();

            String rentalId = rDAO.getId();
            String rentalIdInCache = String.format(RENTAL_CACHE_ENTRY_FORMAT, rentalId);

            jedis.set(rentalIdInCache, new ObjectMapper().writeValueAsString(rDAO));

            return Response.ok(rentalId).build();

        } catch (CosmosException e) {
            if (e.getStatusCode() == 404)
                return Response.status(Response.Status.NOT_FOUND).build();
        }catch (NoSuchElementException e) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.serverError().build();
    }


    @PUT
    @Path("/{" + RENTAL_ID + "}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateRental(@PathParam(HOUSE_ID) String houseId, @PathParam(RENTAL_ID) String rentalId, Rental rental) {
        try(Jedis jedis = RedisCache.getCachePool().getResource()) {

            HouseDAO hDAO = existentHouse(jedis, houseId, db);

            RentalDAO rDAO = new RentalDAO(rental);
            rDAO.setId(rentalId);

            rDAO.setHouseId(houseId);
            rDAO.setPrice(hDAO.getPrice());

            rDAO = db.updateRental(rDAO).getItem();

            String rentalIdInCache = String.format(RENTAL_CACHE_ENTRY_FORMAT, rentalId);
            jedis.set(rentalIdInCache, new ObjectMapper().writeValueAsString(rDAO));

            return Response.ok(rDAO.toRental()).build();

        } catch (CosmosException e) {
            if (e.getStatusCode() == 404) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (NotAuthorizedException e) {
            return Response.status(Response.Status.FORBIDDEN).build();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.serverError().build();
    }

    @GET
    @Path("/{" + RENTAL_ID + "}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRental(@PathParam(HOUSE_ID) String houseId, @PathParam(RENTAL_ID) String rentalId) {
        try(Jedis jedis = RedisCache.getCachePool().getResource()) {

            existentHouse(jedis, houseId, db);

            String rentalIdInCache = String.format(RENTAL_CACHE_ENTRY_FORMAT, rentalId);
            ObjectMapper mapper = new ObjectMapper();
            RentalDAO rDAO;

            String res = jedis.get(rentalIdInCache);
            if (res != null)
                rDAO = mapper.readValue(res, RentalDAO.class);
            else
                rDAO = db.getRentalById(rentalId).getItem();

            jedis.set(rentalIdInCache, mapper.writeValueAsString(rDAO));
            return Response.ok(rDAO.toRental()).build();

        } catch (CosmosException e) {
            if (e.getStatusCode() == 404)
                return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.serverError().build();
    }

}
