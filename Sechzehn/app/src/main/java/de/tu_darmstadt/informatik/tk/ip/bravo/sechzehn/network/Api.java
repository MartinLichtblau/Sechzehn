package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by JohannesTP on 28.06.2017.
 */

public class Api {
    private static final Api ourInstance = new Api();
    private Webservice webservice;

    public static Webservice getInstance() {
        return ourInstance.webservice;
    }

    private Api() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://iptk.herokuapp.com/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        webservice = retrofit.create(Webservice.class);
    }

}
