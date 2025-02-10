package dev.vality.adapter.flow.lib.constant;

public enum Status {

    /**
     * The operation will complete successfully or move on to the next step if a step exists.
     */
    SUCCESS,
    /**
     * The operation will fail and exit.
     */
    ERROR,
    /**
     * The operation will redirect if there are options for the redirect.
     */
    NEED_REDIRECT,
    /**
     * This status need for cycle or retry operation (example: polling status).
     */
    NEED_RETRY

}
