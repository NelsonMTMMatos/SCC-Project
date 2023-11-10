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

    @FunctionName("cosmosDBtest")
    public void updateMostRecentUsers(@CosmosDBTrigger(name = "cosmosTest",
    										databaseName = "scc24db60665",
    										collectionName = "users",
    										preferredLocations="West Europe",
    										createLeaseCollectionIfNotExists = true,
    										connectionStringSetting = "AccountEndpoint=https://scc2460665.documents.azure.com:443/;AccountKey=R60ncQXbxnDD7gAoap4d4zQQUUlPRhPHgzyUFAHCMhZBkZnz8dfiYIa24m4pkv2mhtfbjmxG1ml5ACDbWnJ4OA==;")
        							String[] users,
        							final ExecutionContext context ) {
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			jedis.incr("cnt:cosmos");
			for( String u : users) {
				jedis.lpush("serverless::cosmos::users", u);
			}
			jedis.ltrim("serverless::cosmos::users", 0, 9);
		}
    }

}
