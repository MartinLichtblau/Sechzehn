package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.services;

import android.support.annotation.NonNull;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Friendship;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Pagination;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.UserToken;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.ServiceGenerator;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Provides interaction with the Sechzehn API related to Users.
 * Created by marti on 28.06.2017.
 */

public interface UserService {

    /*Users Collection >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/

    /**
     * Retrieve the user's profile information. The shown information depends on the relationship
     * between the authenticated user and the requested user.
     *
     * @param username username of the User
     * @return Call which results in a {@link User} object.
     */
    @GET("users/{username}")
    Call<User> getUser(
            @NonNull @Path("username") String username
    );

    /**
     * Get a paginated list of users for optional search parameters. The default sorting order is by
     * username ascending.
     * <p>
     * The field distance is only visible if lat and lng is provided. In this case, the sort
     * ordering is by distance ascending.
     * <p>
     * The field similarity is only visible if query is provided. In this case, the sort ordering is
     * by similarity descending. similarity is in range between 0 (worst) and 1 (best).
     *
     * @param page     The page number. Default: 1.
     * @param perPage  The items per page. Default: 10.
     * @param lat      The latitude of the point to search around, can only be used together with
     *                 Tng.
     * @param lng      The longitude of the point to search around, can only be used together with
     *                 lat.
     * @param radius   The search radius around the point in km, can only be used together with lat
     *                 and lng. Default: 10.
     * @param isFriend List only friends or not-friends.
     * @param query    The search parameter for username and real_name.
     * @return Call which results in a page of {@link User} objects.
     */
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

    /**
     * Creates a new user from the given user object.
     *
     * @param user The user object. For the User creation, only email, username and password are
     *             needed.
     * @return Call which results in a {@link UserToken}.
     */
    @POST("users")
    Call<UserToken> createUser(@Body User user);

    /**
     * Update the user's profile information except the username, email address, the password and
     * the current location.
     * <p>
     * The user must be authenticated to edit his profile. It is not possible to edit other
     * profiles.
     *
     * @param username The username of the user.
     * @param user     The {@link User} object containing the updates.
     * @return Call which results in the updated {@link User} object.
     * @see #changePassword(String, RequestBody)
     * @see #changeEmail(String, RequestBody)
     * @see #updateLocation(String, RequestBody)
     * @see #changePicture(String, MultipartBody.Part)
     */
    @PATCH("users/{username}")
    Call<User> updateUser(
            @Path("username") String username,
            @Body User user
    );

    /**
     * Update a users password by sending the old password and the new password.
     *
     * @param username The username of the User
     * @param body     An object containing old_password and password.
     *                 Example: { "old_password": "123", "password": "abc" }
     * @return Call which results in the {@link User} object of the updated user.
     */
    @PATCH("users/{username}/password")
    Call<User> changePassword(
            @Path("username") String username,
            @Body RequestBody body
    );

    /**
     * Request a password reset link that is send to the user's email.
     *
     * @param body An object containing the user's email.
     *             Example: { "email": "a@a.de" }
     * @return Call which results in a message.
     * @deprecated Use {@link LoginService#requestResetPassword(User)} instead.
     */
    @Deprecated
    @POST("/reset")
    Call<Object> resetPassword(
            @Body RequestBody body
    );

    /**
     * Update the user's email address. Must be confirmed by the user's password.
     *
     * @param username The username of the User.
     * @param body     An object containing email and the user's password.
     *                 Example: { "email": "a@a.de", "password": "123"}
     * @return Call which results in the {@link User} object of the updated user.
     */
    @PATCH("users/{username}/email")
    Call<User> changeEmail(
            @Path("username") String username,
            @Body RequestBody body
    );

    /**
     * Delete a user. Needs do be confirmed by the User's password.
     * <p>
     * Uses @{@link HTTP}, because @{@link DELETE} does not support a @{@link Body}.
     *
     * @param username The username of the User.
     * @param body     An object containing the password.
     *                 Example: { "password": "123" }
     * @return Call which results in a message.
     */
    /* See why > https://github.com/square/retrofit/issues/1451*/
    @HTTP(method = "DELETE", path = "users/{username}", hasBody = true)
    Call<Object> deleteAccount(
            @Path("username") String username,
            @Body RequestBody body
    );

    /**
     * Update a user's profile picture by sending a multipart form with name = profile_picture and
     * the image. If the image is not defined, the user's profile picture is set to null (=
     * deleted).
     *
     * @param username The username of the User.
     * @param picture  A {@link okhttp3.MultipartBody.Part} which contains the profile picture.
     * @return Call which results in the {@link User} object of the updated user.
     */
    @Multipart
    @PATCH("users/{username}/profile_picture")
    Call<User> changePicture(
            @Path("username") String username,
            @Part MultipartBody.Part picture
    );

    /**
     * Update a users location. On success, the response is empty.
     *
     * @param username The username of the User.
     * @param location An object containing lat and lng.
     *                 Example: { "lat": "42.42", "lng": "8.6" }
     * @return Call without result.
     */
    @PATCH("users/{username}/location")
    Call<Void> updateLocation(
            @Path("username") String username,
            @Body RequestBody location
    );

    /*Friendships >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/

    /**
     * Retrieve a list of Friends of the given user.
     *
     * @param username The username of the User.
     * @param page     The page number.
     * @param per_page The number of elements per page.
     * @return Call which results in a page of friends.
     * @deprecated Use {@link FriendshipService#getFriends(String, Integer, Integer)} instead.
     */
    @Deprecated
    @GET("users/{username}/friends")
    Call<Pagination<User>> getFriends(
            @Path("username") @NonNull String username,
            @Query("page") Integer page,
            @Query("per_page") Integer per_page
    );

    /**
     * List pending friend requests.
     * <p>
     * When somebody asks the authenticated User, the status is RELATED_CONFIRMED
     * (because the other, the related User raised the friend requests and hence confirmed the
     * friendship) Otherwise, when the authenticated User has asked the related User, the status is
     * RELATING_CONFIRMED.
     *
     * @param username The username of the User.
     * @param page     The page number.
     * @param per_page The number of elements per page.
     * @return Call which results in a page of friendship requests.
     * @deprecated Use {@link FriendshipService#getFriendshipRequests(String, Integer, Integer,
     * Boolean)} instead.
     */
    @Deprecated
    @GET("users/{username}/friends/requests")
    Call<Object> getFriendshipRequests(
            @Path("username") @NonNull String username,
            @Query("page") Integer page,
            @Query("per_page") Integer per_page
    );

    /**
     * Raise a friend request.
     * <p>
     * The requesting User is determined by the authorization token.
     *
     * @param username The username references the User which friendship should be requested.
     * @return Call which results in a friendship object.
     * @deprecated Use {@link FriendshipService#requestFriendship(String)} instead.
     */
    @Deprecated
    @POST("users/{username}/friends")
    Call<Object> requestFriendship(
            @Path("username") @NonNull String username
    );

    /**
     * Confirm or decline a friend request.
     *
     * @param username The username of the User.
     * @param body     An object containing a status which can be CONFIRMED or DECLINED.
     * @return Call which results in a friendship object with the new status.
     * @deprecated Use {@link FriendshipService#answerFriendship(String, Friendship)}
     */
    @Deprecated
    @PATCH("users/{username}/friends")
    Call<Object> answerFriendship(
            @Path("username") @NonNull String username,
            @Body RequestBody body
    );

    /**
     * Remove the Friendship to the specified user.
     *
     * @param username The username of the user.
     * @return Call which results in a message.
     * @deprecated Use {@link FriendshipService#deleteFriendship(String)} instead.
     */
    @Deprecated
    @DELETE("users/{username}/friends")
    Call<Object> deleteFriendship(
            @Path("username") @NonNull String username
    );

    /**
     * An instance of the {@link UserService}.
     */
    UserService UserService = ServiceGenerator.createService(UserService.class);


}