package name.lorenzani.andrea.locationexplorer.exception;

public class FoursquareException extends RuntimeException {

    private final String requestId;

    public FoursquareException(String requestId, String message, Throwable cause) {
        super(message, cause);
        this.requestId = requestId;
    }

    public String getRequestId() {
        return requestId;
    }
}
