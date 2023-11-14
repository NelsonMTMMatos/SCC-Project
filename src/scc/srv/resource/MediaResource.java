package scc.srv.resource;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import scc.utils.Hash;

/**
 * Resource for managing media files, such as images.
 */
@Path("/media")
public class MediaResource
{
	public final String storageConnectionString = System.getenv("BlobStoreConnection");

	BlobContainerClient containerClient;

	public MediaResource(){
		containerClient = new BlobContainerClientBuilder()
				.connectionString(storageConnectionString)
				.containerName("images")
				.buildClient();
	}
	/**
	 * Post a new image.The id of the image is its hash.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.APPLICATION_JSON)
	public Response upload(byte[] contents) {
		String filename = Hash.of(contents) + ".jpg";
		try{
			BlobClient blob = containerClient.getBlobClient(filename);

			blob.upload(BinaryData.fromBytes(contents));
		}catch( Exception e) {
			e.printStackTrace();
		}

		return Response.ok(filename).build();
	}

	/**
	 * Return the contents of an image. Throw an appropriate error message if
	 * id does not exist.
	 */
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public byte[] download(@PathParam("id") String id) {
		try{
			BlobClient blob = containerClient.getBlobClient(id);

			if(!blob.exists()) throw new NotFoundException();

			return blob.downloadContent().toBytes();
		}catch( Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}