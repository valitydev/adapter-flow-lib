package dev.vality.adapter.flow.lib.exception;

public class UnknownHandlerForStepException extends RuntimeException {
    public UnknownHandlerForStepException() {
    }

    public UnknownHandlerForStepException(String message) {
        super(message);
    }

    public UnknownHandlerForStepException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownHandlerForStepException(Throwable cause) {
        super(cause);
    }

    public UnknownHandlerForStepException(String message, Throwable cause, boolean enableSuppression,
                                          boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
