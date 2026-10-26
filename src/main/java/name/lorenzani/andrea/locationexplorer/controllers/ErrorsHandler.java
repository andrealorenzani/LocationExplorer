package name.lorenzani.andrea.locationexplorer.controllers;

import com.fasterxml.jackson.annotation.JsonInclude;
import name.lorenzani.andrea.locationexplorer.exception.FoursquareException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class ErrorsHandler {

    /** The body of an error: a code, the request id (it is also in the logs), what happened and why. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    record ErrorReply(String errorCode, String requestId, String message, String cause) {
    }

    @ExceptionHandler(FoursquareException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    ErrorReply foursquare(FoursquareException ex) {
        return new ErrorReply("1", ex.getRequestId(), ex.getMessage(),
                ex.getCause() == null ? null : ex.getCause().getMessage());
    }
}
