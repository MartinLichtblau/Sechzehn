package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by JohannesTP on 21.06.2017.
 */

public interface Webservice {
    @POST("/users")
    Call<UserToken> createUser(@Body User user);

}
