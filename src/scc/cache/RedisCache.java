package scc.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import scc.authentication.Session;
import scc.utils.Helpers;

public class RedisCache {
	private static final String RedisHostname = System.getenv("REDIS_URL");
	private static final String RedisKey = System.getenv("REDIS_KEY");

	private static final String SESSION_CACHE_ENTRY_FORMAT = "session:%s";
	
	private static JedisPool instance;
	
	public synchronized static JedisPool getCachePool() {
		if( instance != null)
			return instance;
		final JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxTotal(128);
		poolConfig.setMaxIdle(128);
		poolConfig.setMinIdle(16);
		poolConfig.setTestOnBorrow(true);
		poolConfig.setTestOnReturn(true);
		poolConfig.setTestWhileIdle(true);
		poolConfig.setNumTestsPerEvictionRun(3);
		poolConfig.setBlockWhenExhausted(true);
		instance = new JedisPool(poolConfig, RedisHostname, 6380, 1000, RedisKey, true);
		return instance;
		
	}

	public static void putSession(Session session){
		try(Jedis jedis = instance.getResource()){
			jedis.set(String.format(SESSION_CACHE_ENTRY_FORMAT, session.getId()), Helpers.serialize(session));
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public static Session getSession(String uid) throws CacheException{
		try(Jedis jedis = instance.getResource()){

			String res = jedis.get(String.format(SESSION_CACHE_ENTRY_FORMAT, uid));

			if(res == null) throw new CacheException("No entry found.");

			return  new ObjectMapper().readValue(res, Session.class);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}
