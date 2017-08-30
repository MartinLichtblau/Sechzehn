package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This class represents a venue's category.
 */
@Deprecated
public class VenueCategory {

    @SerializedName("name")
    @Expose
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
