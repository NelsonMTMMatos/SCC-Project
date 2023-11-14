package scc.serverless;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.CosmosDBTrigger;
import com.microsoft.azure.functions.annotation.FunctionName;
import redis.clients.jedis.Jedis;
import scc.cache.RedisCache;

/**
 * Azure Functions with Timer Trigger.
 */
public class CosmosDBFunction {
    @FunctionName("TenMostRecentHouses")
    public void updateMostRecentHouses(@CosmosDBTrigger(name = "mostRecentHouses",
    										databaseName = "scc24db60665",
    										collectionName = "houses",
    										preferredLocations="West Europe",
    										createLeaseCollectionIfNotExists = true,
    										connectionStringSetting = "AzureCosmosDBConnection") 
        							String[] houses,
        							final ExecutionContext context ) {
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			for( String h : houses) {
				jedis.lpush("serverless::cosmos::houses", h);
			}
			jedis.ltrim("serverless::cosmos::houses", 0, 9);
		}
    }

}
