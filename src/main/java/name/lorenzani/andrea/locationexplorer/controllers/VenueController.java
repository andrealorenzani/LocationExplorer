package name.lorenzani.andrea.locationexplorer.controllers;

import name.lorenzani.andrea.locationexplorer.foursquare.FoursquareClient;
import name.lorenzani.andrea.locationexplorer.model.VenueByLocationResponse;
import name.lorenzani.andrea.locationexplorer.service.VenueService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VenueController {

    private final VenueService venues;
    private final FoursquareClient client;

    public VenueController(VenueService venues, FoursquareClient client) {
        this.venues = venues;
        this.client = client;
    }

    @GetMapping("/")
    public VenueByLocationResponse index(@RequestParam("loc") String loc) {
        return venues.search(loc, null, null);
    }

    @GetMapping("/search/{location}")
    public VenueByLocationResponse locationInPath(@PathVariable String location) {
        return venues.search(location, null, null);
    }

    @GetMapping("/search/{location}/{limit}")
    public VenueByLocationResponse locationLimited(@PathVariable String location, @PathVariable Integer limit) {
        return venues.search(location, null, limit);
    }

    @GetMapping("/search/{location}/{query}/{limit}")
    public VenueByLocationResponse locationAndTypeLimited(@PathVariable String location,
                                                          @PathVariable String query,
                                                          @PathVariable Integer limit) {
        return venues.search(location, query, limit);
    }

    /** The URL that would be invoked on Foursquare (the secret is hidden). Mainly for debugging. */
    @GetMapping("/query/{location}")
    public String basicPath(@PathVariable String location) {
        return client.describeExploreUrl(location);
    }
}
