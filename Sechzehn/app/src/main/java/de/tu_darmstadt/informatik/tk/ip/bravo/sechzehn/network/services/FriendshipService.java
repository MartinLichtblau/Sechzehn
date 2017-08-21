package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.services;

import android.support.annotation.NonNull;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Friendship;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Pagination;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.ServiceGenerator;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.ApiMessage;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Provides interaction with the Sechzehn API related to Friendships.
 *
 * @author Alexander Geiß on 10.08.2017.
 */

public interface FriendshipService {
    /**
     * Retrieve a list of Friends of the given user.
     *
     * @param username The username of the User.
     * @param page     The page number.
     * @param perPage  The number of elements per page.
     * @return Call which results in a page of friends.
     */
    @GET("users/{username}/friends")
    Call<Pagination<User>> getFriends(
            @Path("username") @NonNull String username,
            @Query("page") Integer page,
            @Query("per_page") Integer perPage
    );

    /**
     * List pending friend requests.
     * <p>
     * When somebody asks the authenticated User, the status is {@link
     * de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Friendship.Status#RELATED_CONFIRMED}
     * (because the other, the related User raised the friend requests and hence confirmed the
     * friendship) Otherwise, when the authenticated User has asked the related User, the status is
     * {@link de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Friendship.Status#RELATING_CONFIRMED}.
     *
     * @param username     The username of the User.
     * @param page         The page number.
     * @param perPage      The number of elements per page.
     * @param onlyIncoming Show only incoming requests that the authenticated user can
     *                     accept/cancel.
     * @return Call which results in a page of friendship requests.
     */
    @GET("users/{username}/friends/requests")
    Call<Pagination<Friendship>> getFriendshipRequests(
            @Path("username") @NonNull String username,
            @Query("page") Integer page,
            @Query("per_page") Integer perPage,
            @Query("only_incoming") Boolean onlyIncoming
    );

    /**
     * Raise a friend request.
     * <p>
     * The requesting User is determined by the authorization token.
     *
     * @param username The username references the User which friendship should be requested.
     * @return Call which results in a {@link Friendship} object.
     */
    @POST("users/{username}/friends")
    Call<Friendship> requestFriendship(
            @Path("username") @NonNull String username
    );

    /**
     * Confirm or decline a friend request.
     *
     * @param username The username of the User.
     * @param body     A {@link Friendship} object. {@link Friendship#status}
     *                 can be {@link de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Friendship.Status#CONFIRMED}
     *                 or {@link de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Friendship.Status#DECLINED}.
     * @return Call which results in a {@link Friendship} object with the new {@link
     * de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Friendship.Status}.
     */
    @PATCH("users/{username}/friends")
    Call<Friendship> answerFriendship(
            @Path("username") @NonNull String username,
            @Body Friendship body
    );

    /**
     * Remove the Friendship to the specified user.
     *
     * @param username The username of the user.
     * @return Call which results in an {@link ApiMessage}
     */
    @DELETE("users/{username}/friends")
    Call<ApiMessage> deleteFriendship(
            @Path("username") @NonNull String username
    );

    /**
     * An instance of the {@link FriendshipService}.
     */
    FriendshipService FriendshipService = ServiceGenerator.createService(FriendshipService.class);
}
