package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network;

import java.io.IOException;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.SzUtils;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by marti on 28.06.2017.
 */

public class AuthenticationInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();

        Request.Builder builder = original.newBuilder()
                .header("Authorization", "Bearer " + SzUtils.getToken()); //Token goes in Header named "Authorization" in our case not "Authentication"

        Request request = builder.build();
        return chain.proceed(request);
    }
}
