package scc.utils;

import jakarta.ws.rs.core.Cookie;
import org.mindrot.jbcrypt.BCrypt;
import scc.authentication.Session;
import scc.cache.CacheException;
import scc.cache.RedisCache;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Helpers {

    public static final boolean AUTH_ON = false;

    private Helpers(){}

    public static String encrypt(String password){
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public static Session checkCookieUser(Cookie session, String id) {
        if (session == null || session.getValue() == null)
            return null;
        Session s;
        try{
            s = RedisCache.getSession(session.getValue());
        } catch (CacheException e) {
            return null;
        }
        if (s == null || s.getUser() == null || s.getUser().isEmpty())
            return null;
        if (!s.getUser().equals(id))
            return null;
        return s;
    }

    public static String toISO8601String(String date){
        return LocalDate.parse(date, DateTimeFormatter.ISO_DATE).toString();
    }

}
