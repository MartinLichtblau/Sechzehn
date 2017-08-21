package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Venue {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("lat")
    @Expose
    private Double lat;
    @SerializedName("lng")
    @Expose
    private Double lng;
    @SerializedName("price")
    @Expose
    private Integer price;
    @SerializedName("foursquare_rating")
    @Expose
    private Double foursquareRating;
    @SerializedName("category")
    @Expose
    private VenueCategory category;
    @SerializedName("similarity")
    @Expose
    private Double similarity;
    @SerializedName("distance")
    @Expose
    private Double distance;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Double getFoursquareRating() {
        return foursquareRating;
    }

    public void setFoursquareRating(Double foursquareRating) {
        this.foursquareRating = foursquareRating;
    }

    public VenueCategory getCategory() {
        return category;
    }

    public void setCategory(VenueCategory category) {
        this.category = category;
    }

    public Double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(Double similarity) {
        this.similarity = similarity;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

}
