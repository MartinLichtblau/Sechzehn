package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.services;

import android.support.annotation.NonNull;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Friendship;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Pagination;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.ServiceGenerator;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.ApiMessage;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * @author Alexander Geiß on 10.08.2017.
 */

public interface FriendshipService {
    @GET("users/{username}/friends")
    Call<Pagination<User>> getFriends(
            @Path("username") @NonNull String username,
            @Query("page") Integer page,
            @Query("per_page") Integer per_page
    );

    @GET("users/{username}/friends/requests")
    Call<Pagination<Friendship>> getFriendshipRequests(
            @Path("username") @NonNull String username,
            @Query("page") Integer page,
            @Query("per_page") Integer per_page
    );

    @POST("users/{username}/friends")
    Call<Friendship> requestFriendship(
            @Path("username") @NonNull String username
    );

    @PATCH("users/{username}/friends")
    Call<Friendship> answerFriendship(
            @Path("username") @NonNull String username,
            @Body Friendship body
    );

    @DELETE("users/{username}/friends")
    Call<ApiMessage> deleteFriendship(
            @Path("username") @NonNull String username
    );

    FriendshipService FriendshipService = ServiceGenerator.createService(FriendshipService.class);
}
