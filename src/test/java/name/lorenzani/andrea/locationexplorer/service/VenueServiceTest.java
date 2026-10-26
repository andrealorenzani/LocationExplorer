package name.lorenzani.andrea.locationexplorer.service;

import name.lorenzani.andrea.locationexplorer.config.FoursquareProperties;
import name.lorenzani.andrea.locationexplorer.exception.FoursquareException;
import name.lorenzani.andrea.locationexplorer.foursquare.FoursquareClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

class VenueServiceTest {

    private MockRestServiceServer server;
    private VenueService service;

    @BeforeEach
    void setup() {
        var builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).ignoreExpectOrder(true).build();
        // pages of 10 venues, at most 3 requests per search: 30 venues
        var properties = new FoursquareProperties("https://foursquare.test/v2/", "venues/explore",
                "id", "secret", "20170916", 10, 3);
        service = new VenueService(new FoursquareClient(builder, properties), properties);
    }

    /** An answer that claims {@code total} venues exist and contains {@code count} of them. */
    private static String page(int total, int firstIndex, int count) {
        String items = IntStream.range(firstIndex, firstIndex + count)
                .mapToObj(i -> "{\"venue\":{\"name\":\"venue" + i + "\"}}")
                .collect(Collectors.joining(","));
        return """
                {"response":{"geocode":{"where":"w","displayString":"W, Italy"},"headerFullLocation":"W",
                "headerLocationGranularity":"city","totalResults":%d,
                "groups":[{"type":"Recommended Places","items":[%s]}]}}""".formatted(total, items);
    }

    private void expectPage(String offset, String limit, String body) {
        var expectation = server.expect(ExpectedCount.once(), requestTo(containsString("/venues/explore")))
                .andExpect(queryParam("limit", limit));
        if (offset == null) {
            expectation.andExpect(requestTo(not(containsString("offset="))));
        } else {
            expectation.andExpect(queryParam("offset", offset));
        }
        expectation.andRespond(withSuccess(body, MediaType.APPLICATION_JSON));
    }

    @Test
    void aSinglePageIsEnoughWhenEverythingFitsInIt() {
        server.expect(queryParam("limit", "10")).andRespond(withSuccess(page(4, 0, 4), MediaType.APPLICATION_JSON));

        var reply = service.search("w", null, null);

        assertThat(reply.totalRes()).isEqualTo(4);
        assertThat(reply.location()).isEqualTo("w");
        assertThat(reply.displayLocation()).isEqualTo("W, Italy");
        assertThat(reply.recommendedVenues()).extracting("name")
                .containsExactly("venue0", "venue1", "venue2", "venue3");
        server.verify();
    }

    @Test
    void theNextPagesStartWhereThePreviousEnded() {
        expectPage(null, "10", page(25, 0, 10));
        expectPage("10", "10", page(25, 10, 10));
        expectPage("20", "5", page(25, 20, 5));

        var reply = service.search("w", null, null);

        assertThat(reply.totalRes()).isEqualTo(25);
        assertThat(reply.recommendedVenues()).extracting("name")
                .containsExactlyInAnyOrderElementsOf(IntStream.range(0, 25).mapToObj(i -> "venue" + i).toList());
        server.verify();
    }

    @Test
    void neverAsksForMoreThanTheMaximumPerSearch() {
        expectPage(null, "10", page(1000, 0, 10));
        expectPage("10", "10", page(1000, 10, 10));
        expectPage("20", "10", page(1000, 20, 10));

        assertThat(service.search("w", null, null).totalRes()).isEqualTo(30);
        server.verify();
    }

    @Test
    void theLimitOfTheCallerWins() {
        expectPage(null, "10", page(100, 0, 10));
        expectPage("10", "2", page(100, 10, 2));

        assertThat(service.search("w", null, 12).totalRes()).isEqualTo(12);
        server.verify();
    }

    @Test
    void aSmallLimitNeedsASingleSmallRequest() {
        server.expect(queryParam("limit", "1")).andRespond(withSuccess(page(100, 0, 1), MediaType.APPLICATION_JSON));

        assertThat(service.search("w", null, 1).totalRes()).isEqualTo(1);
        server.verify();
    }

    @Test
    void usesSectionsAndFreeTextQueriesAsAppropriate() {
        server.expect(queryParam("section", "topPicks"))
                .andExpect(requestTo(not(containsString("query="))))
                .andRespond(withSuccess(page(0, 0, 0), MediaType.APPLICATION_JSON));
        server.expect(queryParam("query", "pizza"))
                .andExpect(requestTo(not(containsString("section="))))
                .andRespond(withSuccess(page(0, 0, 0), MediaType.APPLICATION_JSON));

        service.search("w", " TopPicks ", 3);
        service.search("w", "Pizza", 3);

        server.verify();
    }

    @Test
    void filterIsEmptyWithoutQuery() {
        assertThat(VenueService.filterFor(null)).isEmpty();
        assertThat(VenueService.filterFor("  ")).isEmpty();
        assertThat(VenueService.filterFor("coffee")).isEqualTo(Map.of("section", "coffee"));
    }

    @Test
    void wrapsFoursquareErrors() {
        server.expect(requestTo(containsString("/venues/explore"))).andRespond(withBadRequest());

        assertThatThrownBy(() -> service.search("w", null, null))
                .isInstanceOf(FoursquareException.class)
                .hasMessageStartingWith("Error invoking foursquare API")
                .satisfies(e -> assertThat(((FoursquareException) e).getRequestId()).isNotBlank());
    }

    @Test
    void wrapsErrorsOfTheNextPages() {
        expectPage(null, "10", page(25, 0, 10));
        server.expect(queryParam("offset", "10")).andRespond(withBadRequest());
        server.expect(queryParam("offset", "20")).andRespond(withBadRequest());

        assertThatThrownBy(() -> service.search("w", null, null)).isInstanceOf(FoursquareException.class);
    }
}
