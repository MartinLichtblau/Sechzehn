package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data;

import com.google.gson.annotations.SerializedName;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;

import java.util.Date;

/**
 * Created by Alexander Geiß on 03.08.2017.
 */

public class Message implements IMessage {
    public  String id;
    @SerializedName("created_at")
    public Date created;
    @SerializedName("updated_at")
    public Date updated;
    @SerializedName("is_read")
    public boolean isRead;

    public String body;

    public IUser user;

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

    @Override
    public Date getCreatedAt() {
        return created;
    }
}
