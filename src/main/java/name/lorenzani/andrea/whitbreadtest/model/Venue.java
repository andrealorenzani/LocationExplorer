package name.lorenzani.andrea.whitbreadtest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Venue {

    private String name;
    // URL is present in https://developer.foursquare.com/docs/explore#req=venues/explore%3Fll%3D40.7,-74
    // but it is not returned in the API
    private String url;
    private Location location;
    private Double rating;

    public Venue() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }
}
