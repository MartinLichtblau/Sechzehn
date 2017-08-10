package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data;

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
    public ChatUser(User user) {
        this.user = user;
    }

    private User user;

    /**
     *
     * @return Internal User object
     */
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
}
