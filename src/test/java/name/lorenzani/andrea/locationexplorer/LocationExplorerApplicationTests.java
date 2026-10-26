package name.lorenzani.andrea.locationexplorer;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/** The whole application over HTTP, against a fake Foursquare that knows 250 venues. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class LocationExplorerApplicationTests {

    private static final int TOTAL = 250;
    private static final ConcurrentLinkedQueue<Map<String, String>> requests = new ConcurrentLinkedQueue<>();
    private static HttpServer foursquare;

    @BeforeAll
    static void startFakeFoursquare() throws IOException {
        foursquare = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        foursquare.createContext("/v2/venues/explore", exchange -> {
            Map<String, String> params = Arrays.stream(exchange.getRequestURI().getRawQuery().split("&"))
                    .map(pair -> pair.split("=", 2))
                    .collect(Collectors.toMap(p -> p[0], p -> URLDecoder.decode(p[1], StandardCharsets.UTF_8)));
            requests.add(params);
            int offset = Integer.parseInt(params.getOrDefault("offset", "0"));
            int limit = Integer.parseInt(params.get("limit"));
            int count = Math.max(0, Math.min(limit, TOTAL - offset));
            String items = IntStream.range(offset, offset + count)
                    .mapToObj(i -> "{\"venue\":{\"name\":\"venue" + i + "\",\"rating\":7.5,"
                            + "\"location\":{\"city\":\"" + params.get("near") + "\"}}}")
                    .collect(Collectors.joining(","));
            byte[] body = ("{\"response\":{\"geocode\":{\"where\":\"" + params.get("near") + "\"},"
                    + "\"headerFullLocation\":\"full\",\"totalResults\":" + TOTAL + ","
                    + "\"groups\":[{\"type\":\"Recommended Places\",\"items\":[" + items + "]}]}}")
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        foursquare.start();
    }

    @AfterAll
    static void stopFakeFoursquare() {
        foursquare.stop(0);
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("foursquare.base-uri", () -> "http://localhost:" + foursquare.getAddress().getPort() + "/v2/");
        registry.add("foursquare.client-id", () -> "the-id");
        registry.add("foursquare.client-secret", () -> "the-secret");
    }

    @Autowired
    private TestRestTemplate rest;

    @Test
    void collectsTheVenuesOfAllThePagesUpToTheMaximum() {
        requests.clear();

        Map<?, ?> reply = rest.getForObject("/search/New York", Map.class);

        // 250 venues exist; the application is configured to ask pages of 100
        assertThat(reply.get("totalRes")).isEqualTo(TOTAL);
        assertThat((java.util.List<?>) reply.get("recommendedVenues")).hasSize(TOTAL);
        assertThat(reply.get("requestedLocation")).isEqualTo("New York");
        assertThat(reply.get("location")).isEqualTo("New York");
        assertThat(requests).extracting(r -> r.getOrDefault("offset", "0"))
                .containsExactlyInAnyOrder("0", "100", "200");
        assertThat(requests).allSatisfy(r -> {
            assertThat(r).containsEntry("near", "New York").containsEntry("client_id", "the-id");
            assertThat(r).containsEntry("client_secret", "the-secret").containsEntry("v", "20170916");
        });
    }

    @Test
    void sendsTheSectionAndTheLimit() {
        requests.clear();

        Map<?, ?> reply = rest.getForObject("/search/sarzana/food/3", Map.class);

        assertThat(reply.get("totalRes")).isEqualTo(3);
        assertThat(requests).hasSize(1);
        assertThat(requests.peek()).containsEntry("section", "food").containsEntry("limit", "3");
    }

    @Test
    void queryEndpointDoesNotLeakTheSecret() {
        String url = rest.getForObject("/query/london", String.class);

        assertThat(url).contains("near=london").contains("client_id=the-id").doesNotContain("the-secret");
    }

    @Test
    void healthcheck() {
        assertThat(rest.getForObject("/status/", Map.class)).containsEntry("status", 200);
    }
}
