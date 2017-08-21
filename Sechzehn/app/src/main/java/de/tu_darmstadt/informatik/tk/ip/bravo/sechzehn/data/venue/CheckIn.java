package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.venue;

import android.support.annotation.IntRange;

import com.google.gson.annotations.SerializedName;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Venue;

/**
 * @author Alexander Geiß on 21.08.2017.
 */

public class CheckIn {
    public Integer id;
    @SerializedName("created_at")
    public String createdAt;

    public CheckIn() {
    }

    public CheckIn(@IntRange(from = 0, to = 2) Integer rating) {
        this.rating = rating;
    }

    /**
     * The rating. Rating is 0 (bad), 1 (ok) or 2 (good).
     */
    @IntRange(from = 0, to = 2)
    public Integer rating;
    public Venue venue;

}
