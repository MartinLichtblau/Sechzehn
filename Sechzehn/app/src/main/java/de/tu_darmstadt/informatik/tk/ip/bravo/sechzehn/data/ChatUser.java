package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data;

import android.support.annotation.NonNull;

import com.stfalcon.chatkit.commons.models.IUser;

/**
 * Wraps a User object for usage with ChatKit
 * @author Alexander Geiß on 03.08.2017.
 */

public class ChatUser implements IUser {
    /**
     * Creates a new ChatUser fom User
     * @param user User object which will bew wrapped
     */
    public ChatUser(@NonNull User user) {
        this.user = user;
    }

    @NonNull
    private User user;

    /**
     *
     * @return Internal User object
     */
    @NonNull
    public User getUser() {
        return user;
    }

    @Override
    public String getId() {
        return user.getUsername();
    }

    @Override
    public String getName() {
        return user.getRealName();
    }

    @Override
    public String getAvatar() {
        return user.getProfilePicture();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChatUser chatUser = (ChatUser) o;

        return user.equals(chatUser.user);

    }

    @Override
    public int hashCode() {
        return user.hashCode();
    }
}
