package name.lorenzani.andrea.locationexplorer.foursquare;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import name.lorenzani.andrea.locationexplorer.model.RecommendedVenue;

import java.util.List;
import java.util.Optional;

/**
 * The part of the answer of the Foursquare explore API this service cares about. See
 * <a href="https://developer.foursquare.com/docs/venues/explore">the documentation</a>.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FoursquareExplore(Response response) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(GeoCode geocode,
                           String headerFullLocation,
                           String headerLocationGranularity,
                           Long totalResults,
                           List<Group> groups) {
        public Response {
            totalResults = totalResults == null ? 0L : totalResults;
            groups = groups == null ? List.of() : List.copyOf(groups);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GeoCode(String displayString, String where) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Group(String type, List<Item> items) {
        public Group {
            items = items == null ? List.of() : List.copyOf(items);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(Venue venue) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Venue(String name, String url, Location location, Double rating) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Location(String address, String postalCode, String city, String state, String country) {
    }

    public long totalResults() {
        return Optional.ofNullable(response).map(Response::totalResults).orElse(0L);
    }

    public Optional<Response> body() {
        return Optional.ofNullable(response);
    }

    /** The venues of all the groups, each tagged with the type of the group it comes from. */
    public List<RecommendedVenue> venues() {
        return body().stream()
                .flatMap(r -> r.groups().stream())
                .flatMap(group -> group.items().stream().map(item -> RecommendedVenue.of(group.type(), item)))
                .toList();
    }
}
