package name.lorenzani.andrea.whitbreadtest.model;

import java.util.Optional;

public class RecommendedVenue {

    private String venueType;
    private String name;
    private String url;
    private String address;
    private String city;
    private String country;
    private String state;
    private String postalCode;
    private Double rating;

    public RecommendedVenue() {
    }

    public RecommendedVenue(String type, Item source) {
        this.venueType = type;
        Venue venue = Optional.ofNullable(source.getVenue()).orElse(new Venue());
        Location loc = Optional.ofNullable(venue.getLocation()).orElse(new Location());
        this.address = loc.getAddress();
        this.city = loc.getCity();
        this.country = loc.getCountry();
        this.state = loc.getState();
        this.postalCode = loc.getPostalCode();
        this.name = venue.getName();
        this.url = venue.getUrl();
        this.rating = venue.getRating();
    }

    public String getVenueType() {
        return venueType;
    }

    public void setVenueType(String venueType) {
        this.venueType = venueType;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }
}
