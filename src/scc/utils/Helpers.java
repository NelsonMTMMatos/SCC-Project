package scc.utils;

import com.azure.cosmos.util.CosmosPagedIterable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.Cookie;
import redis.clients.jedis.Jedis;
import scc.authentication.Session;
import scc.cache.CacheException;
import scc.cache.RedisCache;

import java.util.Iterator;

public class Helpers {

    private Helpers(){}

    public static <T> T getItem(CosmosPagedIterable<T> resGet){
        Iterator<T> it = resGet.stream().iterator();
        return it.hasNext() ? it.next() : null;
    }

    public static <T> String serialize(T obj) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(obj);
    }
    public static Session checkCookieUser(Cookie session, String id)
            throws NotAuthorizedException {
        if (session == null || session.getValue() == null)
            throw new NotAuthorizedException("No session initialized");
        Session s;
        try{
            s = RedisCache.getSession(session.getValue());
        } catch (CacheException e) {
            throw new NotAuthorizedException("No valid session initialized");
        }
        if (s == null || s.getUser() == null || s.getUser().length() == 0)
            throw new NotAuthorizedException("No valid session initialized");
        if (!s.getUser().equals(id) && !s.getUser().equals("admin"))
            throw new NotAuthorizedException("Invalid user : " + s.getUser());
        return s;
    }
}
