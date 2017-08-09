package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.socket;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Alexander Geiß on 09.08.2017.
 */

public class GsonJsonConverter {
    public static JSONObject convert(JsonObject in) {
        JSONObject out = new JSONObject();
        for (Map.Entry<String, JsonElement> entry : in.entrySet()) {
            try {
                out.accumulate(entry.getKey(), convert(entry.getValue()));
            } catch (JSONException e) {
                throw new JsonParseException(e);
            }
        }
        return out;
    }

    public static JSONArray convert(JsonArray in) {
        LinkedList<Object> items = new LinkedList<>();
        for (JsonElement e : in) {
            items.add(convert(e));
        }
        return new JSONArray(items);
    }

    public static Object convert(JsonNull in) {
        return JSONObject.NULL;
    }

    public static Object convert(JsonPrimitive in) {
        if (in.isString()) {
            return in.getAsString();
        }else if(in.isNumber()){
            return in.getAsNumber();
        } else if (in.isBoolean()) {
            return in.getAsBoolean();
        }
        throw new RuntimeException();
    }


    public static Object convert(JsonElement value) {
        if (value.isJsonObject()) {
            return convert(value.getAsJsonObject());
        } else if (value.isJsonArray()) {
            return convert(value.getAsJsonArray());
        } else if (value.isJsonPrimitive()) {
            return convert(value.getAsJsonPrimitive());
        }else if (value.isJsonNull()) {
            return convert(value.getAsJsonNull());
        }
        throw new RuntimeException();
    }
}
