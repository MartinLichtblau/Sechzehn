package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data;

import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.venue.Category;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.venue.CheckIn;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.venue.Hour;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.venue.UserVisit;

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

    public String getAddressPart1() {
        if (address == null) {
            return null;
        }
        int endOfFirstPart = address.indexOf(';');
        if (endOfFirstPart < 1) {
            return null;
        }
        return address.substring(0, endOfFirstPart);
    }

    public String getAddressPart2() {
        if (address == null) {
            return null;
        }
        int endOfPart1 = address.indexOf(';');
        if (endOfPart1 < 1) {
            return null;
        }
        int endOfPart2 = address.indexOf(';', endOfPart1);
        if (endOfPart2 < 1) {
            return null;
        }
        return address.substring(0, endOfPart2);
    }

    public String description;

    /**
     * The price category.
     * Must be between 1 and 5, including those.
     */
    @IntRange(from = 1, to = 5)
    public Integer price;
    /**
     * The rating.
     * Must be between 0 and 10.
     */
    @SerializedName("rating")
    @FloatRange(from = 0.0, to = 10.0)
    public Double rating;
    /**
     * The number of ratings.
     */
    @SerializedName("rating_count")
    public Integer ratingCount;

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
     * The top visitors of this location.
     */
    @SerializedName("top_visitors")
    public List<UserVisit> topVisitors;
    /**
     * The opening hours.
     */
    public List<Hour> hours;
    /**
     * The last checkins.
     */
    public List<CheckIn> checkins;

    /**
     * This class represents a venue's section.
     */
    public enum Section {
        @SerializedName("food")FOOD,
        @SerializedName("drinks")DRINKS,
        @SerializedName("coffee")COFFEE,
        @SerializedName("shops")SHOPS,
        @SerializedName("arts")ARTS,
        @SerializedName("outdoors")OUTDOORS,
        @SerializedName("sights")SIGHTS,
        UNKNOWN
    }
}
