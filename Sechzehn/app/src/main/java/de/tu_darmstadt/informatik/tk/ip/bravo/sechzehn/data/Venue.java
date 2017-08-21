package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data;

import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

/**
 * This class represents a venue.
 *
 * @author Alexander Geiß on 20.08.2017.
 */

public class Venue {
    /**
     * The id of the venue.
     */
    public String id;
    /**
     * The name of the venue.
     */
    public String name;
    /**
     * The latitude of the venue.
     *
     * @see #getPosition()
     */
    public Double lat;
    /**
     * The longitude of the venue.
     *
     * @see #getPosition()
     */
    public Double lng;

    /**
     * @return The position of the venue.
     */
    public LatLng getPosition() {
        if (lat == null || lng == null) return null;
        return new LatLng(lat, lng);
    }

    /**
     * The url to the venue website.
     */
    public String url;

    public String phone;
    public String address;
    public String description;

    /**
     * The price category.
     * Must be between 1 and 5, including those.
     */
    @IntRange(from = 1, to = 5)
    public Integer price;
    /**
     * The Foursquare rating.
     * Must be between 0 and 10.
     */
    @SerializedName("foursquare_rating")
    @FloatRange(from = 0.0, to = 10.0)
    public Double foursquareRating;
    /**
     * The category of the venue.
     */
    public Category category;
    /**
     * The similarity to the search query.
     */
    public Double similarity;
    /**
     * The distance from the search location.
     */
    public Double distance;

    /**
     * This class represents a venue's category.
     */
    public static class Category {
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

    /**
     * This class represents a venue's section.
     */
    public static enum Section {
        @SerializedName("food")
        FOOD,
        @SerializedName("drinks")
        DRINKS,
        @SerializedName("coffee")
        COFFEE,
        @SerializedName("shops")
        SHOPS,
        @SerializedName("arts")
        ARTS,
        @SerializedName("outdoors")
        OUTDOORS,
        @SerializedName("sights")
        SIGHTS
    }
}
