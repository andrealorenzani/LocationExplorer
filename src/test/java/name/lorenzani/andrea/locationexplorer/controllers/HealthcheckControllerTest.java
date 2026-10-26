package name.lorenzani.andrea.locationexplorer.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HealthcheckController.class)
class HealthcheckControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void givesBackHealthyStatusWithAndWithoutTrailingSlash() throws Exception {
        for (String path : new String[]{"/status", "/status/"}) {
            mvc.perform(get(path))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("I am alive, location-explorer"));
        }
    }

    @Test
    void failsIfNotAGetRequest() throws Exception {
        mvc.perform(post("/status/")).andExpect(status().isMethodNotAllowed());
    }
}
