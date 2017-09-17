package name.lorenzani.andrea.whitbreadtest.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/status")
public class HealthcheckController {

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public Map<String, Object> status(@Value(value = "${app.name}") String value) {
        Map<String, Object> res = new HashMap<>();
        res.put("status", 200);
        res.put("message", "I am alive, " + value);
        return res;
    }
}




