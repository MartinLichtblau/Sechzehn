package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.venue;

import com.google.gson.annotations.SerializedName;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Venue;

/**
 * @author Alexander Geiß on 21.08.2017.
 */

public class Comment {
    public Integer id;
    public User user;
    public Venue venue;
    public String body;
    public Photo photo;
    public Integer rating;
    /**
     * Date of Creation as String
     */
    @SerializedName("created_at")
    public String created;
}
