package scc.db;

import com.azure.cosmos.*;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import scc.data.HouseDAO;
import scc.data.RentalDAO;

import static scc.db.CosmosDBLayer.*;

public class CosmosDBLayerAsync {

    private static CosmosDBLayerAsync instance;

    public static synchronized CosmosDBLayerAsync getInstance() {
        if( instance != null)
            return instance;

        CosmosAsyncClient client = new CosmosClientBuilder()
                .endpoint(CONNECTION_URL)
                .key(DB_KEY)
                //.directMode()
                .gatewayMode()
                // replace by .directMode() for better performance
                .consistencyLevel(ConsistencyLevel.SESSION)
                .connectionSharingAcrossClientsEnabled(true)
                .contentResponseOnWriteEnabled(true)
                .buildAsyncClient();
        instance = new CosmosDBLayerAsync(client);
        return instance;

    }

    private CosmosAsyncClient  client;
    private CosmosAsyncDatabase db;
    private CosmosAsyncContainer houses, rentals;

    public CosmosDBLayerAsync(CosmosAsyncClient client) {
        this.client = client;
    }

    protected synchronized void init() {
        if( db != null)
            return;
        db = client.getDatabase(DB_NAME);
        houses = db.getContainer("houses");
        rentals = db.getContainer("rentals");
    }

    public void replaceHouse(HouseDAO house) {
        init();
        PartitionKey key = new PartitionKey(house.getId());
        houses.replaceItem(house, house.getId(), key, new CosmosItemRequestOptions()).subscribe();
    }

    public void replaceRental(RentalDAO rental) {
        init();
        PartitionKey key = new PartitionKey(rental.getId());
        rentals.replaceItem(rental, rental.getId(), key, new CosmosItemRequestOptions()).subscribe();
    }

}
