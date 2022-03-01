package dev.vality.adapter.flow.lib.utils;

public class TimeoutUtils {

    public static int toSeconds(int timerRedirectTimeoutMin) {
        return timerRedirectTimeoutMin * 60;
    }

}
