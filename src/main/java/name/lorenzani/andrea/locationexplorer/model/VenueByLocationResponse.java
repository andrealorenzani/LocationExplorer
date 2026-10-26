package name.lorenzani.andrea.locationexplorer.model;

import name.lorenzani.andrea.locationexplorer.foursquare.FoursquareExplore;
import name.lorenzani.andrea.locationexplorer.foursquare.FoursquareExplore.GeoCode;

import java.util.List;
import java.util.Optional;

public record VenueByLocationResponse(String requestedLocation,
                                      String location,
                                      String headerFullLocation,
                                      String displayLocation,
                                      String granularity,
                                      int totalRes,
                                      List<RecommendedVenue> recommendedVenues) {

    /**
     * @param firstPage the first answer of Foursquare, which provides the description of the place
     * @param venues    the venues of all the answers
     */
    public static VenueByLocationResponse of(String requestedLocation, FoursquareExplore firstPage,
                                             List<RecommendedVenue> venues) {
        var body = firstPage.body();
        var geocode = body.map(FoursquareExplore.Response::geocode);
        return new VenueByLocationResponse(
                requestedLocation,
                geocode.map(GeoCode::where).orElse(null),
                body.map(FoursquareExplore.Response::headerFullLocation).orElse(null),
                geocode.map(GeoCode::displayString).orElse(null),
                body.map(FoursquareExplore.Response::headerLocationGranularity).orElse(null),
                venues.size(),
                venues);
    }
}
