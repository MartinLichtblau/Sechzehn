package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data;

import com.google.gson.annotations.SerializedName;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.SzUtils;

/**
 * @author Alexander Geiß on 19.08.2017.
 */

public class LastChat {
    public User user;
    @SerializedName("last_message")
    public Message lastMessage;

    public String getNameOfSenderOfLastMessage() {
        if (lastMessage.sender.equals(SzUtils.getOwnername())) {
            return "Me";
        } else if (lastMessage.sender.equals(user.getUsername())) {
            return user.getOptionalRealName();
        }
        throw new AssertionError();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LastChat lastChat = (LastChat) o;

        if (user != null ? !user.equals(lastChat.user) : lastChat.user != null) return false;
        return lastMessage != null ? lastMessage.equals(lastChat.lastMessage) : lastChat.lastMessage == null;

    }

    @Override
    public int hashCode() {
        int result = user != null ? user.hashCode() : 0;
        result = 31 * result + (lastMessage != null ? lastMessage.hashCode() : 0);
        return result;
    }
}
