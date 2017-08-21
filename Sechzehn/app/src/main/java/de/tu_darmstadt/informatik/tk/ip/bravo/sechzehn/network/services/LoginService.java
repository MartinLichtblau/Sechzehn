package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.services;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.UserToken;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.ServiceGenerator;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.ApiMessage;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Provides interaction with the Sechzehn API related to Authentication.
 * Created by JohannesTP on 28.06.2017.
 */

public interface LoginService {
    /**
     * Retrieve a JWT token that is needed in header for all operations that require authorization.
     *
     * @param user An user object containing email and password.
     * @return Call which results in a {@link UserToken}.
     */
    @POST("/login")
    Call<UserToken> login(@Body User user);

    /**
     * Request a password reset link that is send to the user's email.
     *
     * @param user An user object containing the user's email.
     * @return Call which results in an {@link ApiMessage}.
     */
    @POST("/reset")
    Call<ApiMessage> requestResetPassword(@Body User user);

    /**
     * Confirm a password request by posting the new password.
     *
     * @param token The reset token.
     * @param user  An user object containing the new password.
     * @return Call which results in an {@link ApiMessage}.
     */
    @POST("/reset/{token}")
    Call<ApiMessage> confirmResetPassword(@Path("token") String token, @Body User user);

    /**
     * Confirm an email address.
     *
     * @param token The confirmation token.
     * @return Call which results in an {@link ApiMessage}.
     */
    @GET("/confirm/{token}")
    Call<ApiMessage> confirmEmail(@Path("token") String token);

    /**
     * An instance of the {@link LoginService}.
     */
    LoginService LoginService = ServiceGenerator.createService(LoginService.class);

}
