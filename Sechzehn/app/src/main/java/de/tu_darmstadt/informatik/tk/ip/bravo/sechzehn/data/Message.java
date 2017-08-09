package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data;

import com.google.gson.annotations.SerializedName;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Alexander Geiß on 03.08.2017.
 */

public class Message implements IMessage {
    public  String id;
    @SerializedName("created_at")
    public String created;
    @SerializedName("updated_at")
    public String updated;
    @SerializedName("is_read")
    public boolean isRead;

    public String sender;

    public String  receiver;

    public String body;

    public IUser user;

    public Message(){}
    public Message(String sender, String receiver, String body) {
        this.sender = sender;
        this.receiver = receiver;
        this.body = body;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getText() {
        return body;
    }

    @Override
    public IUser getUser() {
        return user;
    }
    private static final SimpleDateFormat parser=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Override
    public Date getCreatedAt() {

        try {
            return parser.parse(created);
        } catch (ParseException e) {
            return null;
        }
    }
}
