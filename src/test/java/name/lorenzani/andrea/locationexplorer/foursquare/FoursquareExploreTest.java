package name.lorenzani.andrea.locationexplorer.foursquare;

import name.lorenzani.andrea.locationexplorer.model.RecommendedVenue;
import name.lorenzani.andrea.locationexplorer.model.VenueByLocationResponse;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FoursquareExploreTest {

    private final JsonMapper mapper = JsonMapper.builder().build();

    static String sample() throws IOException {
        try (var in = FoursquareExploreTest.class.getResourceAsStream("/foursquare-explore-sarzana.json")) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    void readsAnEmptyAnswer() {
        var explore = mapper.readValue("""
                {"response":{"geocode": {}, "headerLocation": null, "headerFullLocation": null,
                 "headerLocationGranularity": null, "totalResults": 0, "groups": []}}""", FoursquareExplore.class);

        assertThat(explore.response()).isNotNull();
        assertThat(explore.totalResults()).isZero();
        assertThat(explore.response().headerFullLocation()).isNull();
        assertThat(explore.response().geocode()).isNotNull();
        assertThat(explore.venues()).isEmpty();
    }

    @Test
    void missingTotalResultsAndGroupsDefaultToZeroAndEmpty() {
        var explore = mapper.readValue("{\"response\":{}}", FoursquareExplore.class);

        assertThat(explore.totalResults()).isZero();
        assertThat(explore.venues()).isEmpty();
    }

    @Test
    void readsARealAnswerIgnoringWhatIsNotNeeded() throws IOException {
        var explore = mapper.readValue(sample(), FoursquareExplore.class);

        assertThat(explore.totalResults()).isEqualTo(53);
        assertThat(explore.response().headerFullLocation()).isEqualTo("Sarzana");
        assertThat(explore.response().groups().getFirst().items().getFirst().venue().name()).isEqualTo("Gemmi");
    }

    @Test
    void convertsARealAnswerIntoTheReplyOfTheService() throws IOException {
        var explore = mapper.readValue(sample(), FoursquareExplore.class);

        var reply = VenueByLocationResponse.of("", explore, explore.venues());

        assertThat(mapper.writeValueAsString(reply)).isEqualTo("""
                {"requestedLocation":"","location":"sarzana","headerFullLocation":"Sarzana",\
                "displayLocation":"Sarzana, Liguria, Italy","granularity":"city","totalRes":1,\
                "recommendedVenues":[{"venueType":"Recommended Places","name":"Gemmi","url":null,\
                "address":"Via Giuseppe Mazzini, 21","city":"Sarzana","country":"Italia","state":"Liguria",\
                "postalCode":"19038","rating":8.5}]}""");
    }

    @Test
    void anItemWithoutVenueBecomesAnEmptyRecommendation() {
        var venue = RecommendedVenue.of("type", new FoursquareExplore.Item(null));

        assertThat(venue).isEqualTo(new RecommendedVenue("type", null, null, null, null, null, null, null, null));
    }

    @Test
    void theReplyForAnUnparsableAnswerHasNoDetails() {
        var reply = VenueByLocationResponse.of("here", new FoursquareExplore(null), List.of());

        assertThat(reply.requestedLocation()).isEqualTo("here");
        assertThat(reply.location()).isNull();
        assertThat(reply.totalRes()).isZero();
    }
}
