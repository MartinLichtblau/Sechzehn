package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.venue;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.BR;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Venue;

/**
 * @author Alexander Geiß on 21.08.2017.
 */

public class Comment {
    public Integer id;
    public User user;
    public Venue venue;

    @NonNull
    public String body = "";
    public Photo photo;
    /**
     * The id of the accompanying photo.
     * For new Comments only.
     */
    @SerializedName("photo_id")
    public Integer photoId;
    /**
     * The Rating of this comment.
     */
    public Integer rating;
    /**
     * Date of Creation as String
     */
    @SerializedName("created_at")
    public String created;

    public Comment(Integer rating) {
        this.rating = rating;
    }

    public Comment() {
    }
}
