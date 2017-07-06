package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data;

/**
 * Created by JohannesTP on 28.06.2017.
 */

public class UserToken {
    public String token;
    public User user;

    public UserToken(String token, User user) {
        this.token = token;
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }


}
