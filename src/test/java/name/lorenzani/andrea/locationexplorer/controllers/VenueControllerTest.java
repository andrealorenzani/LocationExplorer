package name.lorenzani.andrea.locationexplorer.controllers;

import name.lorenzani.andrea.locationexplorer.exception.FoursquareException;
import name.lorenzani.andrea.locationexplorer.foursquare.FoursquareClient;
import name.lorenzani.andrea.locationexplorer.model.RecommendedVenue;
import name.lorenzani.andrea.locationexplorer.model.VenueByLocationResponse;
import name.lorenzani.andrea.locationexplorer.service.VenueService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VenueController.class)
class VenueControllerTest {

    private static final VenueByLocationResponse EMPTY =
            new VenueByLocationResponse("sarzana", null, null, null, null, 0, List.of());

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private VenueService venues;

    @MockitoBean
    private FoursquareClient client;

    @Test
    void returnsTheUrlThatWouldBeInvoked() throws Exception {
        when(client.describeExploreUrl("sarzana")).thenReturn("url");

        mvc.perform(get("/query/sarzana")).andExpect(status().isOk()).andExpect(content().string("url"));
    }

    @Test
    void searchesByPathAndByParameter() throws Exception {
        when(venues.search("sarzana", null, null)).thenReturn(EMPTY);

        for (String path : new String[]{"/search/sarzana", "/?loc=sarzana"}) {
            mvc.perform(get(path))
                    .andExpect(status().isOk())
                    .andExpect(content().json("""
                            {"requestedLocation":"sarzana","location":null,"headerFullLocation":null,
                             "displayLocation":null,"granularity":null,"totalRes":0,"recommendedVenues":[]}""", true));
        }
    }

    @Test
    void passesLimitAndQuery() throws Exception {
        var venue = new RecommendedVenue("Recommended Places", "Gemmi", null, null, null, null, null, null, 8.5);
        when(venues.search("london", null, 1)).thenReturn(EMPTY);
        when(venues.search("london", "food", 3))
                .thenReturn(new VenueByLocationResponse("london", null, null, null, null, 1, List.of(venue)));

        mvc.perform(get("/search/london/1")).andExpect(status().isOk());
        mvc.perform(get("/search/london/food/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRes").value(1))
                .andExpect(jsonPath("$.recommendedVenues[0].name").value("Gemmi"))
                .andExpect(jsonPath("$.recommendedVenues[0].rating").value(8.5));
    }

    @Test
    void rejectsAMissingLocationAndANonNumericLimit() throws Exception {
        mvc.perform(get("/")).andExpect(status().isBadRequest());
        mvc.perform(get("/search/london/many")).andExpect(status().isBadRequest());
    }

    @Test
    void reportsFoursquareFailuresAsJson() throws Exception {
        when(venues.search("sarzana", null, null)).thenThrow(
                new FoursquareException("req-1", "Error invoking foursquare API: boom", new RuntimeException("boom")));

        mvc.perform(get("/search/sarzana"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("1"))
                .andExpect(jsonPath("$.requestId").value("req-1"))
                .andExpect(jsonPath("$.cause").value("boom"));
    }

    @Test
    void reportsFoursquareFailuresWithoutCause() throws Exception {
        when(venues.search("sarzana", null, null)).thenThrow(new FoursquareException("req-2", "no cause", null));

        mvc.perform(get("/search/sarzana"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("no cause"))
                .andExpect(jsonPath("$.cause").doesNotExist());
    }
}
