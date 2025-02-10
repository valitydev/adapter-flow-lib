package dev.vality.adapter.flow.lib.service;

import dev.vality.adapter.flow.lib.model.PollingInfo;
import dev.vality.adapter.flow.lib.utils.backoff.BackOffExecution;
import dev.vality.adapter.flow.lib.utils.backoff.ExponentialBackOff;
import dev.vality.adapter.flow.lib.utils.backoff.TimeOptionsExtractors;

import java.time.Instant;
import java.util.Map;

import static dev.vality.adapter.flow.lib.utils.backoff.ExponentialBackOff.*;


public class ExponentialBackOffPollingService {

    public BackOffExecution prepareBackOffExecution(PollingInfo pollingInfo, Map<String, String> options) {
        return exponentialBackOff(pollingInfo, options)
                .start();
    }

    public int prepareNextPollingInterval(PollingInfo pollingInfo, Map<String, String> options) {
        return exponentialBackOff(pollingInfo, options)
                .start()
                .nextBackOff()
                .intValue();
    }

    private ExponentialBackOff exponentialBackOff(PollingInfo pollingInfo, Map<String, String> options) {
        final Long currentLocalTime = Instant.now().toEpochMilli();

        Long startTime = pollingInfo.getStartDateTimePolling() != null
                ? pollingInfo.getStartDateTimePolling().toEpochMilli()
                : currentLocalTime;
        Integer exponential = TimeOptionsExtractors.extractExponent(options, DEFAULT_MUTIPLIER);
        Integer defaultInitialExponential =
                TimeOptionsExtractors.extractDefaultInitialExponential(options, DEFAULT_INITIAL_INTERVAL_SEC);
        Integer maxTimeBackOff = TimeOptionsExtractors.extractMaxTimeBackOff(options, DEFAULT_MAX_INTERVAL_SEC);

        return new ExponentialBackOff(
                startTime,
                currentLocalTime,
                exponential,
                defaultInitialExponential,
                maxTimeBackOff);
    }
}