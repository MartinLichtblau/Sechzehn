package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.services;

import java.security.acl.LastOwnerException;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Pagination;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.UserToken;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.ServiceGenerator;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by JohannesTP on 28.06.2017.
 */

public interface LoginService {
    @POST("/login")
    Call<UserToken> login(@Body User u);

    @POST("/reset")
    Call<Object> resetPassword(@Body User u);

    @POST("/reset/{token}")
    Call<Object> resetPasswordConfirm(@Path("token") String token, @Body User u);

    @GET("/confirm/{token}")
    Call<Object> confirmEmail(@Path("token") String token);

    LoginService LoginService = ServiceGenerator.createService(LoginService.class);

}
