package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.venue;

import com.google.gson.annotations.SerializedName;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Venue;

/**
 * This class represents a photo of a venue.
 * @author Alexander Geiß on 24.08.2017.
 */

public class Photo {
    public Integer id;
    @SerializedName("created_at")
    public String createdAt;

    public String url;

    public Venue venue;
    public User user;

}
