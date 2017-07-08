package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network;

import android.support.v4.util.ArrayMap;

import org.json.JSONObject;

import java.util.Map;

import okhttp3.RequestBody;

/**
 * Created by marti on 08.07.2017.
 */

/*
Generate a RequestBody with one or mulitple <Key value> pairs
 e.g. RequestBody body = new GenericBody().put("password", password).put("email", email).generate();
 */
public class GenericBody {
    private Map<String, Object> map = new ArrayMap<>();

    public GenericBody put(String k, String v){
        map.put(k,v);
        return this;
    }

    //Ref.:
    // > https://stackoverflow.com/questions/21398598/how-to-post-raw-whole-json-in-the-body-of-a-retrofit-request
    // > https://stackoverflow.com/questions/12155800/how-to-convert-hashmap-to-json-object-in-java
    public RequestBody generate(){
        return RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"),(new JSONObject(map)).toString());
    }
}
