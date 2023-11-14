package scc.utils;

import com.azure.cosmos.models.CosmosItemResponse;
import scc.data.UserDAO;
import scc.db.CosmosDBLayer;

import java.util.Locale;

/**
 * Standalone program for accessing the database
 *
 */
public class TestUsers
{
	public static void main(String[] args) {
		System.setProperty(org.slf4j.simple.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "Error");

		try {
			Locale.setDefault(Locale.US);
			CosmosDBLayer db = CosmosDBLayer.getInstance();
			String id = "0:" + System.currentTimeMillis();
			CosmosItemResponse<UserDAO> res = null;
			UserDAO u = new UserDAO();
			u.setId(id);
			u.setName("SCC " + id);
			u.setPwd("super_secret");
			u.setPhotoId("0:34253455");

			res = db.createUser(u);
			System.out.println( "Put result");
			System.out.println( res.getStatusCode());
			System.out.println( res.getItem());

			System.out.println( "Get for id = " + id);
			System.out.println( db.getUserById(id) );


			System.out.println( "Get for all ids");
			for( UserDAO e: db.getUsers())
				System.out.println( e);


			// Now, let's create and delete
			id = "0:" + System.currentTimeMillis();
			res = null;
			u = new UserDAO();
			u.setId(id);
			u.setName("SCC " + id);
			u.setPwd("super_secret");
			u.setPhotoId("0:34253455");

			res = db.createUser(u);
			System.out.println( "Put result");
			System.out.println( res.getStatusCode());
			System.out.println( res.getItem());
			System.out.println( "Get for id = " + id);

			System.out.println( "Get by id result");

			System.out.println( db.getUserById(id) );

			System.out.println( "Delte user");
			db.delUserById(id);

			System.out.println( "Get by id result");
			System.out.println( db.getUserById(id) );

			db.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}


