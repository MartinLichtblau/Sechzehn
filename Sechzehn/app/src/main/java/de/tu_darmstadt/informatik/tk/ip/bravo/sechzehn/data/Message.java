package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data;

import com.google.gson.annotations.SerializedName;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Represents a Message
 * <p>
 * @author Alexander Geiß on 03.08.2017.
 */

public class Message implements IMessage {
    private static final SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    /**
     * Message ID
     */
    public int id;
    /**
     * Date of Creation as String
     */
    @SerializedName("created_at")
    public String created;
    /**
     * Date of last update as String
     */
    @SerializedName("updated_at")
    public String updated;
    /**
     * True if the message was seen, else false.
     */
    @SerializedName("is_read")
    public boolean isRead;
    /**
     * Username of the sender
     */
    public String sender;
    /**
     * Username of the reciever
     */
    public String receiver;
    /**
     * Message Body/Text
     */
    public String body;
    /**
     * ChatUser object of the sender
     */
    public ChatUser senderUser;

    /**
     * Creates an empty new Message
     */
    public Message() {
    }

    /**
     * Create a new Message
     *
     * @param sender   from sender
     * @param receiver to reciever
     * @param body     with text message.
     */
    public Message(String sender, String receiver, String body) {
        this.sender = sender;
        this.receiver = receiver;
        this.body = body;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", created='" + created + '\'' +
                ", updated='" + updated + '\'' +
                ", isRead=" + isRead +
                ", sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", body='" + body + '\'' +
                '}';
    }

    @Override
    public String getId() {
        return String.valueOf(id);
    }

    @Override
    public String getText() {
        return body;
    }

    @Override
    public IUser getUser() {
        if (senderUser == null) {
            throw new NullPointerException("The User of this message is not set. It is required to set it.");
        }
        return senderUser;
    }

    @Override
    public Date getCreatedAt() {
        try {
            return parser.parse(created);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * @return The Date of the last update as Date
     */
    public Date getUpdatedAt() {
        try {
            return parser.parse(updated);
        } catch (ParseException e) {
            return null;
        }
    }
}
