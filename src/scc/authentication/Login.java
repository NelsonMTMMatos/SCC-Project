package scc.authentication;

public class Login {

    private String username;
    private String password;

    public Login(){}

    public Login(String username, String password){
        this.username = username;
        this.password = password;
    }


    public String getUsername(){
        return username;
    }

    public String getPassword(){
        return password;
    }

    @Override
    public String toString() {
        return "Login [username=" + username + ", password=" + password + "]";
    }
}
