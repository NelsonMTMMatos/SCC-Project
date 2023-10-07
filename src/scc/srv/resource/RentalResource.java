package scc.srv.resource;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import scc.data.Rental;
import scc.data.User;
import scc.db.CosmosDBLayer;
import scc.utils.Hash;

import java.util.HashMap;

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
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_JSON)
    public String createRental(Rental rental) {
        try {
            CosmosDBLayer db = CosmosDBLayer.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_JSON)
    public String updateRental(@PathParam("id") String id, Rental rental) {
        try {
            CosmosDBLayer db = CosmosDBLayer.getInstance();
        } catch (Exception e) {
            System.err.println(e.toString());
        }
        return null;
    }

    @GET
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_JSON)
    public String getInfoRental(@PathParam("id") String id) {
        try {
            CosmosDBLayer db = CosmosDBLayer.getInstance();
        } catch (Exception e) {
            System.err.println(e.toString());
        }
        return null;
    }



}
