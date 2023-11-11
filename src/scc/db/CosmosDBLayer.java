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

import scc.data.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CosmosDBLayer {
	private static final String CONNECTION_URL = System.getenv("COSMOSDB_URL");
	private static final String DB_KEY = System.getenv("COSMOSDB_KEY");
	private static final String DB_NAME = System.getenv("COSMOSDB_DATABASE");

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
	private CosmosContainer users, houses, rentals, periods, questions;
	
	public CosmosDBLayer(CosmosClient client) {
		this.client = client;
	}
	
	private synchronized void init() {
		if( db != null)
			return;
		db = client.getDatabase(DB_NAME);
		users = db.getContainer("users");
		houses = db.getContainer("houses");
		rentals = db.getContainer("rentals");
		periods = db.getContainer("periods");
		questions = db.getContainer("questions");
		
	}

	public CosmosItemResponse<UserDAO> createUser(UserDAO user) {
		init();
		return users.createItem(user);
	}

	public CosmosItemResponse<Object> delUserById(String id) {
		init();
		PartitionKey key = new PartitionKey( id);
		return users.deleteItem(id, key, new CosmosItemRequestOptions());
	}

	public CosmosItemResponse<UserDAO> updateUser(UserDAO user){
		init();
		PartitionKey key = new PartitionKey(user.getId());
		return users.replaceItem(user, user.getId(), key, new CosmosItemRequestOptions());
	}
	
	public CosmosPagedIterable<UserDAO> getUserById(String id) {
		init();
		return users.queryItems("SELECT * FROM users WHERE users.id=\"" + id + "\"", new CosmosQueryRequestOptions(), UserDAO.class);
	}

	public CosmosPagedIterable<UserDAO> getUsers() {
		init();
		return users.queryItems("SELECT * FROM users ", new CosmosQueryRequestOptions(), UserDAO.class);
	}

	public CosmosPagedIterable<HouseDAO> getHousesOfUser(String id){
		init();
		return houses.queryItems("SELECT * FROM houses WHERE houses.ownerId= \"" + id + "\"", new CosmosQueryRequestOptions(), HouseDAO.class);
	}


	public CosmosItemResponse<HouseDAO> createHouse(HouseDAO house){
		init();
		return houses.createItem(house);
	}

	public CosmosItemResponse<Object> delHouseById (String id){
		init();
		PartitionKey key = new PartitionKey(id);
		return houses.deleteItem(id, key, new CosmosItemRequestOptions());
	}

	public CosmosItemResponse<HouseDAO> updateHouse(HouseDAO house) {
		init();
		PartitionKey key = new PartitionKey(house.getId());
		return houses.replaceItem(house, house.getId(), key, new CosmosItemRequestOptions());
	}

	public CosmosItemResponse<HouseDAO> getHouseById(String id){
		init();
		PartitionKey key = new PartitionKey(id);
		return houses.readItem(id, key, HouseDAO.class);
	//	return  houses.queryItems("SELECT * FROM houses WHERE houses.id=\"" + id + "\"", new CosmosQueryRequestOptions(), HouseDAO.class);
	}

	public CosmosPagedIterable<HouseDAO> getHouses(){
		init();
		return houses.queryItems("SELECT * FROM houses", new CosmosQueryRequestOptions(), HouseDAO.class);
	}

	public CosmosItemResponse<RentalDAO> createRental(RentalDAO rental){
		init();
		return rentals.createItem(rental);
	}

	public CosmosItemResponse<RentalDAO> updateRental(RentalDAO rental){
		init();
		PartitionKey key = new PartitionKey(rental.getId());
		return rentals.replaceItem(rental, rental.getId(), key, new CosmosItemRequestOptions());
	}

	public CosmosItemResponse<RentalDAO> getRentalById(String id){
		init();
		PartitionKey key = new PartitionKey(id);
		return rentals.readItem(id, key, RentalDAO.class);
	//	return rentals.queryItems("SELECT * FROM rentals WHERE rentals.id=\"" + id + "\"", new CosmosQueryRequestOptions(), RentalDAO.class);
	}

	public CosmosPagedIterable<HouseDAO> getHousesByLocation(String location){
		init();
		String query = String.format("SELECT * FROM houses WHERE houses.location=%s", location);
		return houses.queryItems(query, new CosmosQueryRequestOptions(), HouseDAO.class);
	}

	public List<HouseDAO> getHousesByPeriodAndLocation(LocalDate startDate, LocalDate endDate, String location){
		init();
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-ddTHH:mm:ss.fffffffZ");
		String start_date = startDate.format(format);
		String end_date = endDate.format(format);

		String periodQuery = String.format("SELECT DISTINCT periods.house_id FROM periods WHERE NOT (periods.start_date <= %s AND periods.end_date >= %s)", start_date, end_date);

		CosmosPagedIterable<String> houseIds= periods.queryItems(periodQuery, new CosmosQueryRequestOptions(), String.class);

		List<HouseDAO> availableHouses = new ArrayList<>();
		for (String houseId : houseIds) {
			String houseQuery = String.format("SELECT * FROM houses WHERE houses.id = %s AND houses.location = %s", houseId, location);

			CosmosPagedIterable<HouseDAO> houses = this.houses.queryItems(houseQuery, new CosmosQueryRequestOptions(), HouseDAO.class);

			for (HouseDAO house : houses) {
				availableHouses.add(house);
			}
		}

		return availableHouses;
  }
  
	public CosmosItemResponse<PeriodDAO> createPeriod(PeriodDAO period){
		init();
		return periods.createItem(period);
	}

	public CosmosItemResponse<QuestionDAO> createQuestion(QuestionDAO question){
		init();
		return questions.createItem(question);
	}

	public CosmosItemResponse<QuestionDAO> replyToQuestion(QuestionDAO question){
		init();
		PartitionKey key = new PartitionKey(question.getId());
		return questions.replaceItem(question, question.getId(), key, new CosmosItemRequestOptions());
	}

	public CosmosPagedIterable<QuestionDAO> getHouseQuestions(String houseId){
		init();
		String questionsQuery = String.format("SELECT * FROM questions WHERE questions.houseId = %s", houseId);
		return questions.queryItems(questionsQuery, new CosmosQueryRequestOptions(), QuestionDAO.class);
	}

	public CosmosItemResponse<QuestionDAO> getQuestionById(String id){
		init();
		PartitionKey key = new PartitionKey(id);
		return questions.readItem(id, key, QuestionDAO.class);
	//	return questions.queryItems("SELECT * FROM questions WHERE rentals.id=\"" + id + "\"", new CosmosQueryRequestOptions(), QuestionDAO.class);
	}

	public void close() {
		client.close();
	}
	
	
}
