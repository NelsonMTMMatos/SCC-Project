package scc.authentication;

public class Session  {
    private String uid;
    private String user;

    public Session(){}

    public Session(String uid, String user) {
        this.uid = uid;
        this.user = user;
    }

    public String getId() {
        return uid;
    }

    public void setId(String id) {
        this.uid = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Session{" +
                "uid='" + uid + '\'' +
                ", user='" + user + '\'' +
                '}';
    }
}
