package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data;

import com.stfalcon.chatkit.commons.models.IUser;

/**
 * Created by Alexander Geiß on 03.08.2017.
 */

public class ChatUser implements IUser {
    public ChatUser(User user) {
        this.user = user;
    }

    private User user;

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
