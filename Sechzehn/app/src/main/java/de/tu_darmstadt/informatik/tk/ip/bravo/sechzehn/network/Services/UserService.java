package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.Services;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.UserToken;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by marti on 28.06.2017.
 */

public interface UserService {
    /**
     * @GET declares an HTTP GET request
     * @Path("user") annotation on the userId parameter marks it as a
     * replacement for the {user} placeholder in the @GET path
     */
    @GET("users/{username}")
    Call<User> getUser(
            @Path("username") String username
    );

    @POST("users")
    Call<UserToken> createUser(@Body User user);

    @PATCH("users/{username}")
    Call<User> updateUser(
            @Path("username") String username,
            @Body User user
    );

    @PATCH("users/{username}/password")
    Call<User> changePassword(
            @Path("username") String username,
            @Body RequestBody body
    );

    @POST("reset")
    Call<Object> resetPassword(
            @Body RequestBody body
    );

    @PATCH("users/{username}/email")
    Call<User> changeEmail(
            @Path("username") String username,
            @Body RequestBody body
    );

    /* See why > https://github.com/square/retrofit/issues/1451*/
    @HTTP(method = "DELETE", path = "users/{username}", hasBody = true)
    Call<Object> deleteAccount(
            @Path("username") String username,
            @Body RequestBody body
    );
}