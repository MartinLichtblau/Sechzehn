package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.socket;

/**
 * Created by Alexander Geiß on 09.08.2017.
 */

public class ChatSocketException extends RuntimeException {
    public ChatSocketException() {
        super("ChatSocketException");
    }

    public ChatSocketException(String message) {
        super(message);
    }

    public ChatSocketException(String message, Throwable cause) {
        super(message, cause);
    }

    public ChatSocketException(Throwable cause) {
        super(cause);
    }

    public ChatSocketException(Object[] args) {
        super(argsToString(args));
    }

    private static String argsToString(Object[] args) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < args.length; i++) {
            sb.append(args[i]);
            if (i < args.length - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}
