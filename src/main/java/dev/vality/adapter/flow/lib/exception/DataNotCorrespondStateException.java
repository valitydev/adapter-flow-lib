package dev.vality.adapter.flow.lib.exception;

public class DataNotCorrespondStateException extends RuntimeException {

    public DataNotCorrespondStateException() {
    }

    public DataNotCorrespondStateException(String message) {
        super(message);
    }

    public DataNotCorrespondStateException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataNotCorrespondStateException(Throwable cause) {
        super(cause);
    }

    public DataNotCorrespondStateException(String message, Throwable cause, boolean enableSuppression,
                                           boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
