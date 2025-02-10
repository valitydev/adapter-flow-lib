package dev.vality.adapter.flow.lib.exception;

public class DeserializationException extends RuntimeException {
    public DeserializationException() {
        super();
    }

    public DeserializationException(String message) {
        super(message);
    }

    public DeserializationException(Throwable cause) {
        super(cause);
    }

    public DeserializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
