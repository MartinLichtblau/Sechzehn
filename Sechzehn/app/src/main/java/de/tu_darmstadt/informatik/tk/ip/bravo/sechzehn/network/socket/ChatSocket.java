package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.socket;

import android.util.Log;


import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.net.URISyntaxException;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Message;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.SzUtils;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by Alexander Geiß on 09.08.2017.
 */

public class ChatSocket {
    private Socket socket;

    public static final String EVENT_WARNING = "warning";
    public static final String EVENT_MESSAGE = "message";

    public ChatSocket() {
        IO.Options options = new IO.Options();

        options.query = "token=" + SzUtils.getToken();
        try {
            socket = IO.socket("https://iptk.herokuapp.com/messages", options);
            socket.connect()
                    .on(Socket.EVENT_ERROR, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            throw new ChatSocketException("Error Event", new ChatSocketException(args));
                        }
                    })
                    .on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            Log.d("ChatSocket connected", "" + socket.connected());
                        }
                    });
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public Emitter sendMessage(Message m) {
        return socket.emit(EVENT_MESSAGE, messageToJSONObject(m));
    }

    public void setOnMessageListener(final Listener listener) {
        socket.on(EVENT_MESSAGE, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                executeMessageListener(args, listener);
            }
        });
    }

    private static JSONObject messageToJSONObject(Message m) {
        JsonObject gsonMessage = SzUtils.gson.toJsonTree(m).getAsJsonObject();
        return GsonJsonConverter.convert(gsonMessage);
    }

    private static void executeMessageListener(Object[] args, Listener listener) {
        if (args.length == 1) {
            Message m = SzUtils.gson.fromJson(args[0].toString(), Message.class);
            listener.call(m);
        } else {
            throw new ChatSocketException("Listener must have exact one object passed in the args array.");
        }
    }

    public interface Listener {
        public void call(Message msg);
    }


}
