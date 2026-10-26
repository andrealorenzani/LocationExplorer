package name.lorenzani.andrea.locationexplorer.foursquare;

import name.lorenzani.andrea.locationexplorer.config.FoursquareProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class FoursquareClientTest {

    private static final String EMPTY = """
            {"response":{"geocode": {}, "headerLocation": null, "headerFullLocation": "test",
             "headerLocationGranularity": null, "totalResults": 0, "groups": []}}""";

    private static final String BASE = "https://foursquare.test/v2/venues/explore?near=";
    private static final String AUTH = "&client_id=id&client_secret=secret&v=20170916&m=foursquare";

    private MockRestServiceServer server;
    private FoursquareClient client;

    @BeforeEach
    void setup() {
        var builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        var properties = new FoursquareProperties("https://foursquare.test/v2/", "venues/explore",
                "id", "secret", "20170916", 100, 10);
        client = new FoursquareClient(builder, properties);
    }

    @Test
    void callsTheExploreApi() {
        server.expect(requestTo(BASE + "sarzana" + AUTH + "&limit=1"))
                .andRespond(withSuccess(EMPTY, MediaType.APPLICATION_JSON));

        var explore = client.explore("sarzana", Map.of(), 0, 1);

        assertThat(explore.response().headerFullLocation()).isEqualTo("test");
        server.verify();
    }

    @Test
    void sendsOffsetAndFilter() {
        server.expect(requestTo(BASE + "sarzana" + AUTH + "&section=food&offset=100&limit=50"))
                .andRespond(withSuccess(EMPTY, MediaType.APPLICATION_JSON));

        client.explore("sarzana", Map.of("section", "food"), 100, 50);

        server.verify();
    }

    @Test
    void encodesTheLocation() {
        server.expect(requestTo(BASE + "New%20York%26Co" + AUTH + "&limit=1"))
                .andRespond(withSuccess(EMPTY, MediaType.APPLICATION_JSON));

        client.explore("New York&Co", Map.of(), 0, 1);

        server.verify();
    }

    @Test
    void propagatesErrorsOfTheApi() {
        server.expect(requestTo(BASE + "sarzana" + AUTH + "&limit=1")).andRespond(withBadRequest());

        assertThatThrownBy(() -> client.explore("sarzana", Map.of(), 0, 1))
                .isInstanceOf(HttpClientErrorException.class);
    }

    @Test
    void descriptionOfTheUrlHidesTheSecret() {
        String url = client.describeExploreUrl("london");

        assertThat(url).startsWith(BASE + "london").contains("client_id=id").contains("client_secret=REDACTED")
                .doesNotContain("secret=secret");
    }
}
