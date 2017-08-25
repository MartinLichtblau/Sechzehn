package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data;

/**
 * Created by marti on 22.08.2017.
 */

public class VenueSearch {
    public Integer page;
    public Integer perPage;
    public Double lat;
    public Double lng;
    public Double radius;
    public String section; //the section for quick searches Example: food. Possible values:  food , drinks , coffee , shops , arts , outdoors , sights .
    public String query;
    public Integer price; //the price ranking Example: 1. Possible values:  1 , 2 , 3 , 4 , 5 .
    public String time; //time Example: 2017-08-17 15:20.
    public Boolean sortByDistance;
    public Boolean searchHere = true; //default is to search here in this area

    public VenueSearch(Integer page, Integer perPage, Double lat, Double lng, Double radius, String section, String query, Integer price, String time, Boolean sortByDistance) {
        this.page = page;
        this.perPage = (perPage == null ? 25 : perPage);
        this.lat = lat;
        this.lng = lng;
        this.radius = radius;
        this.section = section;
        this.query = query;
        this.price = price;
        this.time = time;
        this.sortByDistance = sortByDistance;
        this.searchHere = searchHere;
    }

    public VenueSearch(Integer perPage){
        this.perPage = (perPage == null ? 25 : perPage);
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPerPage() {
        return perPage;
    }

    public void setPerPage(Integer perPage) {
        this.perPage = perPage;
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

    public Double getRadius() {
        return radius;
    }

    public void setRadius(Double radius) {
        this.radius = radius;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Boolean getSortByDistance() {
        return sortByDistance;
    }

    public void setSortByDistance(Boolean sortByDistance) {
        this.sortByDistance = sortByDistance;
    }

    public Boolean getSearchHere() {
        return searchHere;
    }

    public void setSearchHere(Boolean searchHere) {
        this.searchHere = searchHere;
    }
}
