package dev.vality.adapter.flow.lib.utils;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

@Validated
@Getter
@Setter
@ConfigurationProperties("time.config")
public class TimerProperties {

    @NotNull
    private int redirectTimeoutMin;

    @NotNull
    private int maxTimePollingMin;

    @NotNull
    private int pollingDelayMs;

    @NotNull
    private int exponential;

    @NotNull
    private int defaultInitialExponential;

    @NotNull
    private int maxTimeBackOff;

    @NotNull
    private int maxTimeCoefficient;

}
