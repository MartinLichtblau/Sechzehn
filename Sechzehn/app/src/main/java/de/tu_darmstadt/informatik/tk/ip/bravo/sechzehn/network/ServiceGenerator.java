package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by marti on 28.06.2017.
 */

public class ServiceGenerator {

    private static final String BASE_URL = "https://iptk.herokuapp.com/api/";


    private static final HttpLoggingInterceptor logging = new HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY);

    public static final AuthenticationInterceptor authentication = new AuthenticationInterceptor();

    private static final OkHttpClient httpClient = new OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(authentication)
            .build();

    private static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build();

    public static Retrofit getRetrofit() {
        return retrofit;
    }

    public static <S> S createService(
            Class<S> serviceClass) {
        return retrofit.create(serviceClass);
    }

    public static <S> S createService(
            Class<S> serviceClass, final String authToken) {
        return retrofit.create(serviceClass);
    }
}