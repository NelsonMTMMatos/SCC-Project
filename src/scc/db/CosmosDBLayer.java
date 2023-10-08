package scc.db;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.util.CosmosPagedIterable;

import scc.data.HouseDAO;
import scc.data.UserDAO;

public class CosmosDBLayer {
	private static final String CONNECTION_URL = "https://scc232460665.documents.azure.com:443/";
	private static final String DB_KEY = "3OQYJPlyi5kfhenKoKWuL1iCmeVcIcYnZhIH2TtQhXBIRIqluDTOeX24lMr6TyvIhunvOjOBgk8sACDbissptQ==";
	private static final String DB_NAME = "scc2324";
	
	private static CosmosDBLayer instance;

	public static synchronized CosmosDBLayer getInstance() {
		if( instance != null)
			return instance;

		CosmosClient client = new CosmosClientBuilder()
		         .endpoint(CONNECTION_URL)
		         .key(DB_KEY)
		         //.directMode()
		         .gatewayMode()		
		         // replace by .directMode() for better performance
		         .consistencyLevel(ConsistencyLevel.SESSION)
		         .connectionSharingAcrossClientsEnabled(true)
		         .contentResponseOnWriteEnabled(true)
		         .buildClient();
		instance = new CosmosDBLayer( client);
		return instance;
		
	}
	
	private CosmosClient client;
	private CosmosDatabase db;
	private CosmosContainer users, houses;
	
	public CosmosDBLayer(CosmosClient client) {
		this.client = client;
	}
	
	private synchronized void init() {
		if( db != null)
			return;
		db = client.getDatabase(DB_NAME);
		users = db.getContainer("users");
		houses = db.getContainer("houses");
		
	}

	public CosmosItemResponse<Object> delUserById(String id) {
		init();
		PartitionKey key = new PartitionKey( id);
		return users.deleteItem(id, key, new CosmosItemRequestOptions());
	}
	
	public CosmosItemResponse<UserDAO> putUser(UserDAO user) {
		init();
		return users.createItem(user);
	}
	
	public CosmosPagedIterable<UserDAO> getUserById( String id) {
		init();
		return users.queryItems("SELECT * FROM users WHERE users.id=\"" + id + "\"", new CosmosQueryRequestOptions(), UserDAO.class);
	}

	public CosmosPagedIterable<UserDAO> getUsers() {
		init();
		return users.queryItems("SELECT * FROM users ", new CosmosQueryRequestOptions(), UserDAO.class);
	}

	public CosmosItemResponse<Object> delHouseById (String id){
		init();
		PartitionKey key = new PartitionKey(id);
		return houses.deleteItem(id, key, new CosmosItemRequestOptions());
	}

	public CosmosItemResponse<HouseDAO> createHouse(HouseDAO house){
		init();
		return houses.createItem(house);
	}

	public CosmosPagedIterable<HouseDAO> getHouseById(String id){
		init();
		return  houses.queryItems("SELECT * FROM houses WHERE houses.id=\"" + id + "\"", new CosmosQueryRequestOptions(), HouseDAO.class);
	}

	public CosmosPagedIterable<HouseDAO> getHouses(){
		init();
		return houses.queryItems("SELECT * FROM houses", new CosmosQueryRequestOptions(), HouseDAO.class);
	}

	public void close() {
		client.close();
	}
	
	
}
