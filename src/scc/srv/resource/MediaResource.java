package scc.srv.resource;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import scc.utils.Hash;

import java.util.List;


import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * Resource for managing media files, such as images.
 */
@Path("/media")
public class MediaResource
{
	String storageConnectionString = "DefaultEndpointsProtocol=https;AccountName=sccstwesteurope60483;AccountKey=YoeUZKk0fOUhopzhx+50PGWGGNmxTplkNhJdATvwezG6D3dDdjKMH6GWqVyzr9sSF6QbHa3HUbxy+AStqfvisw==;EndpointSuffix=core.windows.net";

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
	@Path("/")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.APPLICATION_JSON)
	public String upload(byte[] contents) {
		String filename = Hash.of(contents) + ".jpg";
		try{
			BlobClient blob = containerClient.getBlobClient(filename);

			blob.upload(BinaryData.fromBytes(contents));

			System.out.println( "File uploaded : " + filename);
		}catch( Exception e) {
			e.printStackTrace();
		}

		return filename;
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

	/**
	 * Lists the ids of images stored.
	 */
	@GET
	@Path("/list")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> list() {
		return containerClient.listBlobs().stream().map((BlobItem::getName)).toList();
	}

}