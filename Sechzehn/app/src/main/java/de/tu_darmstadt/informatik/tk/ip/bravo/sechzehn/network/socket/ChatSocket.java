package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.socket;

import android.util.Log;

import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.HashMap;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Message;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.ApiMessage;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.SzUtils;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Provides WebSocket communication for chats
 *
 * @author Alexander Geiß on 09.08.2017.
 */

public class ChatSocket {


    public static final String EVENT_WARNING = "warning";
    public static final String EVENT_MESSAGE = "message";
    public static final String EVENT_READ = "read";
    private Socket socket;
    private HashMap<BaseListener, Emitter.Listener> listeners = new HashMap<>();

    /**
     * Creates and connects a new ChatSocket.
     */
    public ChatSocket() {
        String token = SzUtils.getToken();

        IO.Options options = new IO.Options();
        options.query = "token=" + token;
        try {
            socket = IO.socket("https://iptk.herokuapp.com/messages", options);
            enableLogging();

            socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the logging.
     */
    private void enableLogging() {
        socket
                .on(Socket.EVENT_ERROR, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Log.e("ChatSocket", "Error Event: " + argsToString(args));
                        //throw new ChatSocketException("Error Event", new ChatSocketException(args));
                    }
                })
                .on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Log.d("ChatSocket", "Connected: " + socket.connected());
                    }
                })
                .on(ChatSocket.EVENT_WARNING, new Emitter.Listener() {
                    @Override
                    public void call(final Object... args) {
                        Log.w("ChatSocket", EVENT_WARNING + ": " + argsToString(args));
                    }
                })
                .on(ChatSocket.EVENT_MESSAGE, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Log.d("ChatSocket", EVENT_MESSAGE + ": " + argsToString(args));
                    }
                })
                .on(ChatSocket.EVENT_READ, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Log.d("ChatSocket", EVENT_READ + ": " + argsToString(args));
                    }
                });
    }

    /**
     * Converts an args {@link Object}[] to String.
     *
     * @param args Argument array of objects
     * @return A String based on the content of args.
     */
    static String argsToString(Object[] args) {
        StringBuilder sb = new StringBuilder();
        sb.append("<");
        for (int i = 0; i < args.length; i++) {
            sb.append(args[i]);
            if (i < args.length - 1) {
                sb.append(", ");
            }
        }
        sb.append(">");
        return sb.toString();
    }

    /**
     * Converts a {@link Message} object to JSON.
     *
     * @param message The Message.
     * @return The corresponding JSONObject.
     */
    private static JSONObject messageToJSONObject(Message message) {
        JsonObject gsonMessage = SzUtils.gson.toJsonTree(message).getAsJsonObject();
        return GsonJsonConverter.convert(gsonMessage);
    }

    /**
     * Executes the message {@link Listener} after deserializing the {@link Message} object.
     *
     * @param args     Socket event arguments.
     * @param listener The message listener.
     */
    private static void executeMessageListener(Object[] args, Listener listener) {
        if (args.length == 1) {
            Message msg = SzUtils.gson.fromJson(args[0].toString(), Message.class);
            listener.call(msg);
        } else {
            throw new ChatSocketException("Listener must have exact one object passed in the args array.");
        }
    }

    /**
     * Executes the {@link WarningListener} after deserializing the {@link  ApiMessage} object.
     *
     * @param args     Socket event arguments.
     * @param listener The warning listener.
     */
    private void executeWarningListener(Object[] args, WarningListener listener) {
        if (args.length == 1) {
            ApiMessage err = SzUtils.gson.fromJson(args[0].toString(), ApiMessage.class);
            listener.call(err);
        } else {
            throw new ChatSocketException("Listener must have exact one object passed in the args array.");
        }
    }

    /**
     * Sends the {@link Message} m.
     *
     * @param m The message which is sent.
     */
    public void sendMessage(Message m) {
        Log.d("ChatSocket", "Send: " + EVENT_MESSAGE + ": " + m.toString());
        socket.emit(EVENT_MESSAGE, messageToJSONObject(m));
    }

    /**
     * Sends a READ message for the {@link Message} m.
     * And sets {@link Message#isRead} of m to {@link true}.
     *
     * @param m The message for which the READ message is sent.
     */
    public void sendMessageRead(Message m) {
        m.isRead = true;
        Log.d("ChatSocket", "Send: " + EVENT_READ + ": " + m.toString());
        socket.emit(EVENT_READ, messageToJSONObject(m));
    }

    /**
     * Adds a message listener
     *
     * @param listener Listener which is invoked if a message is received.
     */
    public void addOnMessageListener(final Listener listener) {
        Emitter.Listener emitterListener = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                executeMessageListener(args, listener);
            }
        };
        addListener(EVENT_MESSAGE, listener, emitterListener);
    }

    /**
     * Adds a read listener
     *
     * @param listener Listener which is invoked if a message has been read.
     */
    public void addOnReadListener(final Listener listener) {
        Emitter.Listener emitterListener = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                executeMessageListener(args, listener);
            }
        };
        addListener(EVENT_READ, listener, emitterListener);
    }

    /**
     * Adds a warning listener
     *
     * @param listener Listener which is invoked if a warning is received.
     */
    public void addOnWarningListener(final WarningListener listener) {
        Emitter.Listener emitterListener = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                executeWarningListener(args, listener);
            }
        };
        addListener(EVENT_WARNING, listener, emitterListener);
    }


    /**
     * Removes a message listener
     *
     * @param listener Listener to be removed.
     */
    public void removeOnMessageListener(final Listener listener) {
        removeListener(EVENT_MESSAGE, listener);
    }

    /**
     * Removes a read listener
     *
     * @param listener Listener to be removed.
     */
    public void removeOnReadListener(Listener listener) {
        removeListener(EVENT_READ, listener);
    }

    /**
     * Removes a warninglistener
     *
     * @param listener Listener to be removed.
     */
    public void removeOnWarningListener(final WarningListener listener) {
        removeListener(EVENT_WARNING, listener);
    }

    /**
     * Adds a new Listener
     *
     * @param event           for this event.
     * @param listener        The listener.
     * @param emitterListener The underlying {@link io.socket.emitter.Emitter.Listener}.
     */
    private void addListener(String event, final BaseListener listener, Emitter.Listener emitterListener) {
        listeners.put(listener, emitterListener);
        socket.on(event, emitterListener);
    }

    /**
     * Removes a Listener
     *
     * @param event    from this event.
     * @param listener The listener.
     */
    private void removeListener(String event, BaseListener listener) {
        if (!listeners.containsKey(listener)) return;
        socket.off(event, listeners.remove(listener));
    }

    /**
     * Connects the Socket
     */
    public void connect() {
        if (socket.connected()) return;
        socket.connect();
    }

    /**
     * Disconnects the Socket
     */
    public void disconnect() {
        socket.disconnect();
    }

    private interface BaseListener {
    }

    /**
     * Interface for message related listeners
     */
    public interface Listener extends BaseListener {
        public void call(Message msg);
    }

    /**
     * Interface for warning listeners
     */
    public interface WarningListener extends BaseListener {
        public void call(ApiMessage err);
    }


}
