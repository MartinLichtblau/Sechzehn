package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.venue;

import com.google.gson.annotations.SerializedName;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;

/**
 * @author Alexander Geiß on 21.08.2017.
 */

public class CheckIn {
    public Integer id;
    @SerializedName("created_at")
    public String createdAt;
    public Integer rating;
    public User user;
}
