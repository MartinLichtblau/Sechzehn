package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.venue;

import com.google.gson.annotations.SerializedName;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;

/**
 * This class represents visits of a user to a venue.
 * @author Alexander Geiß on 21.08.2017.
 */

public class UserVisit {
    public User user;
    @SerializedName("visit_count")
    public int visitCount;
}
