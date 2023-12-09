package scc.srv.resource;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.io.*;
import scc.utils.Hash;

@Path("/media")
public class MediaResource {

	private final File storageDirectory = new File("/mnt/vol");

	public MediaResource() {}

	@POST
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.APPLICATION_JSON)
	public String upload(byte[] contents) {
		String filename = Hash.of(contents) + ".jpg";
		File file = new File(storageDirectory, filename);

		try (FileOutputStream fos = new FileOutputStream(file)) {
			fos.write(contents);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return filename;
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public byte[] download(@PathParam("id") String id) throws IOException {
		File file = new File(storageDirectory, id);

		if (!file.exists())
			throw new NotFoundException("File not found: " + id);

		try (FileInputStream fis = new FileInputStream(file)) {
			byte[] data = new byte[(int) file.length()];
			fis.read(data);
			return data;
		}
	}
}
