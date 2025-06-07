package github.hjkim27.exception;

public class NetworkInterfaceParseException extends Exception {
    public NetworkInterfaceParseException() {
    }

    public NetworkInterfaceParseException(String message) {
        super(message);
    }

    public NetworkInterfaceParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public NetworkInterfaceParseException(Throwable cause) {
        super(cause);
    }

    public NetworkInterfaceParseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
