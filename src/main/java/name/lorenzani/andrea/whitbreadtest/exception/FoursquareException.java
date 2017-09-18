package name.lorenzani.andrea.whitbreadtest.exception;

public class FoursquareException extends RuntimeException {

    private final String requestId;

    public FoursquareException(String reqid, String message, Throwable cause) {
        super(message, cause);
        this.requestId = reqid;
    }

    public String getRequestId() {
        return requestId;
    }
}
