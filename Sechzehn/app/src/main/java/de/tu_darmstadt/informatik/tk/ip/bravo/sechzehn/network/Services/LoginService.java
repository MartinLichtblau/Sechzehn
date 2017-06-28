package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.Services;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.UserToken;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by JohannesTP on 28.06.2017.
 */

public interface LoginService {
    @POST("/login")
    Call<UserToken> login(@Body User u);
}
