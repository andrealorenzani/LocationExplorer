package name.lorenzani.andrea.whitbreadtest.controllers;

import name.lorenzani.andrea.whitbreadtest.exception.FoursquareException;
import name.lorenzani.andrea.whitbreadtest.model.Response;
import name.lorenzani.andrea.whitbreadtest.model.VenueByLocationResponse;
import name.lorenzani.andrea.whitbreadtest.model.VenueResponse;
import name.lorenzani.andrea.whitbreadtest.utils.FoursquareInvoker;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.*;
import org.junit.runner.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.mock.mockito.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@WebMvcTest(VenueByLocation.class)
public class VenueByLocationResponseTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private RestTemplate template;

    @MockBean
    private FoursquareInvoker fsinvoker;

    private ExecutorService threadpool = Executors.newSingleThreadExecutor();

    @Before
    public void setup() throws Exception {
        Mockito.when(fsinvoker.getExploreUrl(Mockito.anyString())).thenReturn("url");
        Future<VenueResponse> mockResp = threadpool.submit(VenueResponse::new);
        Response emptyRes = new Response();
        emptyRes.setGroups(Collections.emptyList());
        VenueResponse venueRes = new VenueResponse();
        venueRes.setResponse(emptyRes);
        Mockito.when(fsinvoker.invokeExplore("sarzana", 0, "")).thenReturn(venueRes);
    }

    @Test
    public void itCanReturnTheUrl() throws Exception {
        this.mvc.perform(get("/query/sarzana").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(content().string("url"));
    }

    @Test
    public void itCanSearchSarzana() throws Exception {
        this.mvc.perform(get("/search/sarzana").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(content().string("{\"requestedLocation\":\"sarzana\",\"location\":null,\"headerFullLocation\":null,\"displayLocation\":null,\"granularity\":null,\"totalRes\":0,\"recommendedVenues\":[]}"));
    }
}