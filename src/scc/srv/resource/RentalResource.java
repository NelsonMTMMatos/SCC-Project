package scc.srv.resource;

import com.azure.cosmos.util.CosmosPagedIterable;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import scc.data.Rental;
import scc.data.RentalDAO;
import scc.db.CosmosDBLayer;


import java.util.HashMap;
import java.util.Iterator;

@Path("/house/{id}/rental")
//@Path("/house/" + id + "/rental")
public class RentalResource {

    private HashMap<String, Rental> rentals;

    @PathParam("id")
    private String id;

    public RentalResource() {

    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String createRental(Rental rental) {
        try {

            CosmosDBLayer db = CosmosDBLayer.getInstance();
            CosmosPagedIterable<RentalDAO> resGet = db.getRentalById(rental.getId());
            RentalDAO r = getRental(resGet);
            if (r != null) {
                throw new Exception("Rental already exists.");
            }
            db.createRental(new RentalDAO(rental));
            return rental.getId();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String updateRental(@PathParam("id") String id, Rental rental) {
        try {

            CosmosDBLayer db = CosmosDBLayer.getInstance();
            CosmosPagedIterable<RentalDAO> resGet = db.getRentalById(id);
            RentalDAO r = getRental(resGet);
            if (r == null) {
                throw new Exception("Rental doesn't exist.");
            }
            db.updateRental(new RentalDAO(rental));
            return id;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @GET
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String getRentalInfo(@PathParam("id") String id) {
        try {
            CosmosDBLayer db = CosmosDBLayer.getInstance();
            CosmosPagedIterable<RentalDAO> resGet = db.getRentalById(id);
            RentalDAO r = getRental(resGet);
            return r.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private RentalDAO getRental(CosmosPagedIterable<RentalDAO> resGet ){
        Iterator<RentalDAO> it = resGet.stream().iterator();
        return it.hasNext() ? it.next() : null;
    }



}
