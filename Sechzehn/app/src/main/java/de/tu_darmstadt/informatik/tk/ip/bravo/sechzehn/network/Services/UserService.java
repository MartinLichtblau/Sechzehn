package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.Services;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.UserToken;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
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
    @GET("users/{user}")
    Call<User> getUser(
            @Path("user") String userId
    );

    @POST("users")
    Call<UserToken> createUser(@Body User user);
}