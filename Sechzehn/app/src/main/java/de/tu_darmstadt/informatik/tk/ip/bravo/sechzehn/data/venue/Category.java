package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.venue;

import com.google.gson.annotations.SerializedName;

/**
 * This class represents a venue's category.
 *
 * @author Alexander Geiß
 */
public class Category {
    /**
     * The id of the category.
     */
    public String id;
    /**
     * The name of the category.
     */
    public String name;
    /**
     * The short name of the category.
     */
    @SerializedName("short_name")
    public String shortName;
    /**
     * The plural name of the category.
     */
    @SerializedName("plural_name")
    public String pluralName;
    /**
     * The url of the category icon.
     */
    public String icon;

}
