package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Response;

/**
 * Created by marti on 08.07.2017.
 */


// Based on > https://futurestud.io/tutorials/retrofit-2-simple-error-handling
public class ErrorUtils {

    public static APIError parseError(Response<?> response) {
        //Based on https://stackoverflow.com/questions/39837998/custom-message-error-using-retrofit-responsebody
        Type type = new TypeToken<List<APIError>>() {}.getType(); //Because it is not a single APIError but a List [{},{},{}, ...] with one element in our case :-(
        Converter<ResponseBody, List<APIError>> converter =
                ServiceGenerator.getRetrofit()
                        .responseBodyConverter(type, new Annotation[0]);

        APIError error;

        try {
            error = converter.convert(response.errorBody()).get(0); //Only the first ErrorObject in the list (since I have never seen more)
        } catch (IOException e) {
            return new APIError();
        }

        return error;
    }
}