package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.services;

import android.support.annotation.NonNull;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Pagination;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.UserToken;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.ServiceGenerator;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.SzUtils;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by marti on 28.06.2017.
 */

public interface UserService {

    /*Users Collection >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
    @GET("users/{username}")
    Call<User> getUser(
            @Path("username") String username
    );

    @GET("users")
    Call<Pagination<User>> getUsers(
            @Query("page") Integer page,
            @Query("per_page") Integer perPage,
            @Query("lat") Double lat,
            @Query("lng") Double lng,
            @Query("radius") Double radius,
            @Query("is_friend") Boolean isFriend,
            @Query("query") String query);

    /*User >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
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

    @Multipart
    @PATCH("users/{username}/profile_picture")
    Call<User> changePicture(
            @Path("username") String username,
            @Part MultipartBody.Part picture
    );

    @PATCH("users/{username}/location")
    Call<User> updateLocation(
            @Path("username") String username,
            @Body RequestBody location
    );

    /*Friendships >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
    @GET("users/{username}/friends")
    Call<Pagination<User>> getFriends(
            @Path("username") @NonNull String username,
            @Query("page") Integer page,
            @Query("per_page") Integer per_page
    );

    @POST("users/{username}/friends")
    Call<Object> addFriend(
            @Path("username") @NonNull String username
    );

    public UserService UserService = ServiceGenerator.createService(UserService.class);


}