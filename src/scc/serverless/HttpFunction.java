package scc.serverless;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.BindingName;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import scc.cache.RedisCache;
import scc.data.PeriodDAO;
import scc.db.CosmosDBLayer;
import scc.utils.Helpers;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

/**
 * Azure Functions with HTTP Trigger. These functions can be accessed at:
 * {Server_URL}/api/{route}
 * Complete URL appear when deploying functions.
 */
public class HttpFunction {


	@FunctionName("http-discounted")
	public HttpResponseMessage discountedPeriods(@HttpTrigger(name = "req",
			methods = {HttpMethod.GET },
			authLevel = AuthorizationLevel.ANONYMOUS,
			route = "serverless/discountedPeriodsUntil/{date}")
		 HttpRequestMessage<Optional<String>> request,
		 @BindingName("date") String date,
		 final ExecutionContext context) {

		List<PeriodDAO> periods = CosmosDBLayer.getInstance()
				.discountedPeriods(Helpers.toISO8601String(LocalDate.now().toString()), Helpers.toISO8601String(date))
				.stream().toList();
		return request.createResponseBuilder(HttpStatus.OK).body(periods).build();
	}

	@FunctionName("discounted-periods")
	public HttpResponseMessage discountedPeriods(@HttpTrigger(name = "req",
													 methods = {HttpMethod.GET},
													 authLevel = AuthorizationLevel.ANONYMOUS,
													 route = "serverless/discounted")
					HttpRequestMessage<Optional<String>> request,
					final ExecutionContext context) {
		return request.createResponseBuilder(HttpStatus.OK).body("Ola").build();
		/*
		String dateParam = request.getQueryParameters().get("date");

		if (dateParam != null) {
			return request.createResponseBuilder(HttpStatus.OK).body("DateParam: " + dateParam).build();
			/*List<PeriodDAO> periods = CosmosDBLayer.getInstance()
					.discountedPeriods(Helpers.toISO8601String(LocalDate.now().toString()), Helpers.toISO8601String(dateParam))
					.stream().toList();
			//return request.createResponseBuilder(HttpStatus.OK).body(periods).build();
		}

		//return request.createResponseBuilder(HttpStatus.BAD_REQUEST).build();
	*/
	}

	@FunctionName("http-info")
	public HttpResponseMessage info(@HttpTrigger(name = "req", 
										methods = {HttpMethod.GET }, 
										authLevel = AuthorizationLevel.ANONYMOUS,
										route = "serverless/info") 
			HttpRequestMessage<Optional<String>> request,
			final ExecutionContext context) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Headers:\n");
		request.getHeaders().forEach( (k,v) -> { buffer.append( k + "->" + v + "\n");});
		return request.createResponseBuilder(HttpStatus.OK).body(buffer.toString()).build();
	}
	
	@FunctionName("http-stats")
	public HttpResponseMessage run(@HttpTrigger(name = "req", 
										methods = {HttpMethod.GET }, 
										authLevel = AuthorizationLevel.ANONYMOUS,
										route = "serverless/stats") 
			HttpRequestMessage<Optional<String>> request,
			final ExecutionContext context) {
		StringBuffer result = new StringBuffer();
		result.append("Serverless stats: v. 0002 : \n");
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			Long vall = jedis.incr("cnt:http");
			result.append("HTTP functions called ");
			result.append(vall);
			result.append(" times.\n");

			String val = jedis.get("cnt:cosmos");
			if( val == null)
				val = "0";
			result.append("Cosmos functions called ");
			result.append(val);
			result.append(" times.\n");

			val = jedis.get("cnt:blob");
			if( val == null)
				val = "0";
			result.append("Blob functions called ");
			result.append(val);
			result.append(" times.\n");

			val = jedis.get("cnt:timer");
			if( val == null)
				val = "0";
			result.append("Timer functions called ");
			result.append(val);
			result.append(" times.\n");
		}
		return request.createResponseBuilder(HttpStatus.OK).body(result.toString()).build();
	}

	@FunctionName("get-redis")
	public HttpResponseMessage getRedis(@HttpTrigger(name = "req", 
											methods = {HttpMethod.GET }, 
											authLevel = AuthorizationLevel.ANONYMOUS, 
											route = "serverless/redis/{key}") 
				HttpRequestMessage<Optional<String>> request,
				@BindingName("key") String key, 
				final ExecutionContext context) {
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			jedis.incr("cnt:http");
			String val = jedis.get(key);
			return request.createResponseBuilder(HttpStatus.OK).body("GET key = " + key + "; val = " + val).build();
		}
	}

	@FunctionName("lrange-redis")
	public HttpResponseMessage lrangeRedis(@HttpTrigger(name = "req", 
											methods = {HttpMethod.GET }, 
											authLevel = AuthorizationLevel.ANONYMOUS, 
											route = "serverless/redis/lrange/{key}") 
				HttpRequestMessage<Optional<String>> request,
				@BindingName("key") String key, 
				final ExecutionContext context) {
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			jedis.incr("cnt:http");
			List<String> val = jedis.lrange(key, 0, -1);
			return request.createResponseBuilder(HttpStatus.OK).body("GET key = " + key + "; val = " + val).build();
		}
	}

	@FunctionName("set-redis")
	public HttpResponseMessage setRedis(@HttpTrigger(name = "req", 
											methods = {HttpMethod.POST }, 
											authLevel = AuthorizationLevel.ANONYMOUS, 
											route = "serverless/redis/{key}") 
				HttpRequestMessage<Optional<String>> request,
				@BindingName("key") String key, 
				final ExecutionContext context) {
		String val = request.getBody().orElse("");
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			jedis.incr("cnt:http");
			jedis.set(key, val);
			return request.createResponseBuilder(HttpStatus.OK).body("SET key = " + key + "; val = " + val).build();
		}
	}

	@FunctionName("echo")
	public HttpResponseMessage echo(@HttpTrigger(name = "req", 
										methods = {HttpMethod.GET }, 
										authLevel = AuthorizationLevel.ANONYMOUS, 
										route = "serverless/echo/{text}") 
				HttpRequestMessage<Optional<String>> request,
				@BindingName("text") String txt, 
				final ExecutionContext context) {
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			jedis.incr("cnt:http");
		}
		return request.createResponseBuilder(HttpStatus.OK).body(request).build();
	}


	@FunctionName("echo-simple")
	public HttpResponseMessage echoSimple(@HttpTrigger(name = "req", 
											methods = {HttpMethod.GET }, 
											authLevel = AuthorizationLevel.ANONYMOUS, 
											route = "serverless/echosimple/{text}") 
				HttpRequestMessage<Optional<String>> request,
				@BindingName("text") String txt, 
				final ExecutionContext context) {
		return request.createResponseBuilder(HttpStatus.OK).body(txt).build();
	}
}
