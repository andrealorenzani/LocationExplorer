package name.lorenzani.andrea.locationexplorer.foursquare;

import name.lorenzani.andrea.locationexplorer.config.FoursquareProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/** Thin client for the explore endpoint of Foursquare. */
@Component
public class FoursquareClient {

    private final RestClient restClient;
    private final FoursquareProperties properties;

    public FoursquareClient(RestClient.Builder builder, FoursquareProperties properties) {
        this.restClient = builder.baseUrl(properties.baseUri()).build();
        this.properties = properties;
    }

    /**
     * @param filter  the extra parameters ({@code section} or {@code query}) to narrow the search
     * @param offset  how many venues to skip
     * @param limit   how many venues to ask for
     */
    public FoursquareExplore explore(String location, Map<String, String> filter, long offset, long limit) {
        return restClient.get()
                .uri(builder -> exploreUri(builder, location, filter, offset, limit, properties.clientSecret()))
                .retrieve()
                .body(FoursquareExplore.class);
    }

    /** The URL of the first request that {@link #explore} does for the location, with the secret hidden. */
    public String describeExploreUrl(String location) {
        var builder = new DefaultUriBuilderFactory(properties.baseUri()).builder();
        return exploreUri(builder, location, Map.of(), 0, properties.pageSize(), "REDACTED").toString();
    }

    private URI exploreUri(UriBuilder builder, String location, Map<String, String> filter,
                           long offset, long limit, String secret) {
        Map<String, Object> variables = new HashMap<>();
        builder.path(properties.explorePath())
                .queryParam("near", "{near}")
                .queryParam("client_id", "{clientId}")
                .queryParam("client_secret", "{clientSecret}")
                .queryParam("v", "{version}")
                .queryParam("m", "foursquare");
        variables.put("near", location);
        variables.put("clientId", properties.clientId());
        variables.put("clientSecret", secret);
        variables.put("version", properties.version());
        filter.forEach((name, value) -> {
            builder.queryParam(name, "{" + name + "}");
            variables.put(name, value);
        });
        if (offset > 0) {
            builder.queryParam("offset", offset);
        }
        builder.queryParam("limit", limit);
        return builder.build(variables);
    }
}
