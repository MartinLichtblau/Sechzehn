package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.socket;

/**
 * ChatSocket specific exception
 *
 * @author Alexander Geiß on 09.08.2017.
 */

public class ChatSocketException extends RuntimeException {
    /**
     * Constructs a new ChatSocketException
     */
    public ChatSocketException() {
        super("ChatSocketException");
    }

    /**
     * Constructs a new ChatSocketException with a message
     *
     * @param message The detail message.
     */
    public ChatSocketException(String message) {
        super(message);
    }

    /**
     * Constructs a new ChatSocketException with a message and a cause
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public ChatSocketException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new ChatSocketException with a cause
     *
     * @param cause the cause
     */
    public ChatSocketException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new ChatSocketException with a detail Message based on an arguments object
     *
     * @param args Argument object
     */
    public ChatSocketException(Object[] args) {
        super(ChatSocket.argsToString(args));
    }

}
