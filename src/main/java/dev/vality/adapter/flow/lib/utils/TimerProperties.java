package dev.vality.adapter.flow.lib.utils;

import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

@Validated
@Getter
@Setter
public class TimerProperties {

    @NotNull
    private int redirectTimeout;

    @NotNull
    private int maxTimePolling;

    @NotNull
    private int pollingDelay;

    @NotNull
    private int exponential;

    @NotNull
    private int defaultInitialExponential;

    @NotNull
    private int maxTimeBackOff;

    @NotNull
    private int maxTimeCoefficient;

}
