package name.lorenzani.andrea.whitbreadtest.controllers;

import name.lorenzani.andrea.whitbreadtest.exception.FoursquareException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
class FoursquareExceptionHandler {

    @ResponseBody
    @ExceptionHandler(FoursquareException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    Map<String, String> userNotFoundExceptionHandler(FoursquareException ex) {
        Map<String, String> res = new HashMap<>();
        res.put("errorCode", "1");
        res.put("requestId", ex.getRequestId());
        res.put("message", ex.getMessage());
        res.put("cause", ex.getCause().getMessage());
        return res;
    }
}
