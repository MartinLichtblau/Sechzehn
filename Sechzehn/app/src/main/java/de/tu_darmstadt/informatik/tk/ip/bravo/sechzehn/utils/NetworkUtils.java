package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils;

import com.google.gson.reflect.TypeToken;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.ServiceGenerator;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Response;

/**
 * Created by marti on 08.07.2017.
 */
// Based on > https://futurestud.io/tutorials/retrofit-2-simple-error-handling
public class NetworkUtils{
    public static ApiMessage parseError(Response<?> response) {
        ApiMessage error = new ApiMessage();
        ResponseBody errorBody = response.errorBody();  //Body/ErrorBody is consumed on first use, so save it before touching it
        if(errorBody == null)
            return error;
        //Based on https://stackoverflow.com/questions/39837998/custom-message-error-using-retrofit-responsebody
        Type type = new TypeToken<List<ApiMessage>>() {}.getType(); //Because it is not a single ApiMessage but a List [{},{},{}, ...] with one element in our case :-(
        Converter<ResponseBody, List<ApiMessage>> arrayConverter = ServiceGenerator.getRetrofit().responseBodyConverter(type, new Annotation[0]);
        try {
            error = arrayConverter.convert(errorBody).get(0); //Only the first ErrorObject in the list (since I have never seen more)
        } catch (Exception e) {
            error.setMessage("Error: Something bad happened!"); //Probably it's a 500 Server Error returning on JSON Object instead of Array
            e.printStackTrace();
        }

        return error;
    }


}



/*
V2
public class NetworkUtils {

    public static ApiMessage parseError(Response<?> response) {
        ApiMessage error = new ApiMessage();
        ResponseBody errorBody = response.errorBody();  //Body/ErrorBody is consumed on first use, so save it before touching it
        if(errorBody == null)
            return error;
        String errorString = "";
        try {
            errorString = response.errorBody().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("NetworkUtils", ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" +errorString);

        if(errorString.endsWith("]")){
            //It's an array
            //Based on https://stackoverflow.com/questions/39837998/custom-message-error-using-retrofit-responsebody
            Type type = new TypeToken<List<ApiMessage>>() {}.getType(); //Because it is not a single ApiMessage but a List [{},{},{}, ...] with one element in our case :-(
            Converter<ResponseBody, List<ApiMessage>> arrayConverter = ServiceGenerator.getRetrofit().responseBodyConverter(type, new Annotation[0]);
            try {
                error = arrayConverter.convert(errorBody).get(0); //Only the first ErrorObject in the list (since I have never seen more)
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if(errorString.endsWith("}")){
            //It's a single object
            Converter<ResponseBody, ApiMessage> singleConverter = ServiceGenerator.getRetrofit().responseBodyConverter(ApiMessage.class, new Annotation[0]);
            try {
                error = singleConverter.convert(errorBody); //Only the first ErrorObject in the list (since I have never seen more)
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return error;
    }
}*/
