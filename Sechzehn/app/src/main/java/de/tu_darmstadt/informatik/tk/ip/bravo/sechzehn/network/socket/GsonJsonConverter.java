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
 * Converts GSON json trees to org.json json trees
 *
 * @author Alexander Geiß on 09.08.2017.
 */

public class GsonJsonConverter {
    /**
     * Converts a GSON json object to a org.json json object
     *
     * @param in GSON json object
     * @return org.json json object
     */
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

    /**
     * Converts a GSON json array to a org.json json array
     *
     * @param in GSON json array
     * @return org.json json array
     */
    public static JSONArray convert(JsonArray in) {
        LinkedList<Object> items = new LinkedList<>();
        for (JsonElement e : in) {
            items.add(convert(e));
        }
        return new JSONArray(items);
    }

    /**
     * Converts a GSON json null to a org.json json null
     *
     * @param in GSON json null
     * @return org.json json null
     */
    public static Object convert(JsonNull in) {
        return JSONObject.NULL;
    }

    /**
     * Converts a GSON json primitive to a primitive
     *
     * @param in GSON json primitive
     * @return primitive representing an org.json json primitive
     */
    public static Object convert(JsonPrimitive in) {
        if (in.isString()) {
            return in.getAsString();
        } else if (in.isNumber()) {
            return in.getAsNumber();
        } else if (in.isBoolean()) {
            return in.getAsBoolean();
        }
        throw new RuntimeException();
    }

    /**
     * Converts a GSON json element to a object
     *
     * @param in GSON json primitive
     * @return object representing an org.json json element
     */
    public static Object convert(JsonElement in) {
        if (in.isJsonObject()) {
            return convert(in.getAsJsonObject());
        } else if (in.isJsonArray()) {
            return convert(in.getAsJsonArray());
        } else if (in.isJsonPrimitive()) {
            return convert(in.getAsJsonPrimitive());
        } else if (in.isJsonNull()) {
            return convert(in.getAsJsonNull());
        }
        throw new RuntimeException();
    }
}
