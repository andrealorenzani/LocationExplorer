package name.lorenzani.andrea.locationexplorer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param baseUri            e.g. {@code https://api.foursquare.com/v2/}
 * @param explorePath        path of the explore endpoint, relative to the base uri
 * @param clientId           Foursquare credentials: never commit them, provide them through the environment
 * @param clientSecret       see {@code clientId}
 * @param version            the {@code v} parameter: the day on which the behaviour of the API was frozen
 * @param pageSize           how many venues are asked per request (the API allows 100 at most)
 * @param maxRequestsPerSearch cap on the number of requests done to answer one search
 */
@ConfigurationProperties(prefix = "foursquare")
public record FoursquareProperties(String baseUri,
                                   String explorePath,
                                   String clientId,
                                   String clientSecret,
                                   String version,
                                   int pageSize,
                                   int maxRequestsPerSearch) {

    /** The most venues one search can return. */
    public int maxVenuesPerSearch() {
        return pageSize * maxRequestsPerSearch;
    }
}
