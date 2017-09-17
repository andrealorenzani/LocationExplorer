package name.lorenzani.andrea.whitbreadtest.restclient;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class VenueByLocationResponse {
    private String requestedLocation;
    private String location;
    private String headerFullLocation;
    private String displayLocation;
    private String granularity;
    private int totalRes;
    private List<RecommendedVenue> recommendedVenues;

    public VenueByLocationResponse(){}

    public VenueByLocationResponse(String requestedLocation, VenueResponse response) {
        this.requestedLocation=requestedLocation;
        Optional<Response> responseObj = Optional.ofNullable(response.getResponse());
        if(responseObj.isPresent()){
            this.headerFullLocation = responseObj.get().getHeaderFullLocation();
            //this.totalRes = (int)responseObj.get().getTotalResult();
            this.granularity = responseObj.get().getHeaderLocationGranularity();
            Optional<GeoCode> geocode = Optional.ofNullable(responseObj.get().getGeocode());
            if(geocode.isPresent()){
                this.displayLocation = geocode.get().getDisplayString();
                this.location = geocode.get().getWhere();
            }
            List<Group> groups = responseObj.get().getGroups();
            this.recommendedVenues =  groups.stream()
                                            .flatMap(group -> group.getItems()
                                                                   .stream()
                                                                   .map(item -> new RecommendedVenue(group.getType(), item)))
                                            .collect(Collectors.toList());
            this.totalRes = recommendedVenues.size();
        }
    }

    public String getRequestedLocation() {
        return requestedLocation;
    }

    public void setRequestedLocation(String requestedLocation) {
        this.requestedLocation = requestedLocation;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getHeaderFullLocation() {
        return headerFullLocation;
    }

    public void setHeaderFullLocation(String headerFullLocation) {
        this.headerFullLocation = headerFullLocation;
    }

    public String getDisplayLocation() {
        return displayLocation;
    }

    public void setDisplayLocation(String displayLocation) {
        this.displayLocation = displayLocation;
    }

    public String getGranularity() {
        return granularity;
    }

    public void setGranularity(String granularity) {
        this.granularity = granularity;
    }

    public int getTotalRes() {
        return totalRes;
    }

    public void setTotalRes(int totalRes) {
        this.totalRes = totalRes;
    }

    public List<RecommendedVenue> getRecommendedVenues() {
        return recommendedVenues;
    }

    public void setRecommendedVenues(List<RecommendedVenue> recommendedVenues) {
        this.recommendedVenues = recommendedVenues;
    }
}
