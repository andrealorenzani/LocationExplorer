package name.lorenzani.andrea.whitbreadtest.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthcheckController {

    @RequestMapping("/status")
    public Map<String, String> status(@Value(value = "${app.name}") String value) {
        Map<String, String> res = new HashMap<>();
        res.put("status", "200");
        res.put("message", "I am alive, "+value);
        return res;
    }
}




