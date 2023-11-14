package scc.serverless;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.CosmosDBInput;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import scc.cache.RedisCache;
import scc.data.PeriodDAO;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Azure Functions with HTTP Trigger. These functions can be accessed at:
 * {Server_URL}/api/{route}
 * Complete URL appear when deploying functions.
 */
public class HttpFunction {

	@FunctionName("discounted-periods")
	public HttpResponseMessage discountedPeriods(
			@HttpTrigger(
					name = "req",
					methods = {HttpMethod.GET},
					authLevel = AuthorizationLevel.ANONYMOUS,
					route = "serverless/discounted")
			HttpRequestMessage<Optional<String>> request,
			@CosmosDBInput(
					name = "cosmosTest",
					databaseName = "scc24db60665",
					collectionName = "periods",
					connectionStringSetting = "AzureCosmosDBConnection",
					sqlQuery = "SELECT * FROM c WHERE c.discount > 0")
			List<PeriodDAO> periods,
			final ExecutionContext context) {

			String dateParam = request.getQueryParameters().get("date");
			LocalDate now = LocalDate.now();
			LocalDate givenDate = LocalDate.parse(dateParam, DateTimeFormatter.ISO_DATE);

			List<PeriodDAO> filteredPeriods = new ArrayList<>();

			if (periods != null) {
				for (PeriodDAO p : periods) {
					LocalDate startDate = LocalDate.parse(p.getStartDate(), DateTimeFormatter.ISO_DATE);
					LocalDate endDate = LocalDate.parse(p.getEndDate(), DateTimeFormatter.ISO_DATE);
					if (startDate.isAfter(now) && endDate.isBefore(givenDate)) {
						filteredPeriods.add(p);
					}
				}
			}

			return request.createResponseBuilder(HttpStatus.OK).body(filteredPeriods).build();
	}
}