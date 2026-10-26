package name.lorenzani.andrea.locationexplorer.model;

import name.lorenzani.andrea.locationexplorer.foursquare.FoursquareExplore.Item;
import name.lorenzani.andrea.locationexplorer.foursquare.FoursquareExplore.Location;
import name.lorenzani.andrea.locationexplorer.foursquare.FoursquareExplore.Venue;

import java.util.Optional;

public record RecommendedVenue(String venueType,
                               String name,
                               String url,
                               String address,
                               String city,
                               String country,
                               String state,
                               String postalCode,
                               Double rating) {

    public static RecommendedVenue of(String type, Item source) {
        Venue venue = Optional.ofNullable(source.venue()).orElse(new Venue(null, null, null, null));
        Location loc = Optional.ofNullable(venue.location()).orElse(new Location(null, null, null, null, null));
        return new RecommendedVenue(type, venue.name(), venue.url(), loc.address(), loc.city(), loc.country(),
                loc.state(), loc.postalCode(), venue.rating());
    }
}
