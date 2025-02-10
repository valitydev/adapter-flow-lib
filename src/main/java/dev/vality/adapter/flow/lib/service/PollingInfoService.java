package dev.vality.adapter.flow.lib.service;

import dev.vality.adapter.common.damsel.OptionsExtractors;
import dev.vality.adapter.flow.lib.model.EntryStateModel;
import dev.vality.adapter.flow.lib.model.PollingInfo;
import dev.vality.adapter.flow.lib.utils.TimerProperties;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@RequiredArgsConstructor
public class PollingInfoService {

    private final TimerProperties timerProperties;

    public PollingInfo initPollingInfo(EntryStateModel entryStateModel) {
        PollingInfo pollingInfo = entryStateModel.getStartedPollingInfo();
        if (pollingInfo == null) {
            pollingInfo = new PollingInfo();
        }
        if (pollingInfo.getStartDateTimePolling() == null) {
            pollingInfo.setStartDateTimePolling(Instant.now());
        }
        Instant maxDateTimePolling = calcDeadline(entryStateModel, pollingInfo);
        pollingInfo.setMaxDateTimePolling(maxDateTimePolling);
        return pollingInfo;
    }

    public boolean isDeadline(PollingInfo pollingInfo) {
        Instant now = Instant.now();
        return now.isAfter(pollingInfo.getMaxDateTimePolling());
    }

    private Instant calcDeadline(EntryStateModel entryStateModel, @NonNull PollingInfo pollingInfo) {
        if (pollingInfo.getMaxDateTimePolling() == null) {
            Integer maxTimePolling = OptionsExtractors.extractMaxTimePolling(
                    entryStateModel.getBaseRequestModel().getAdapterConfigurations(),
                    timerProperties.getMaxTimePollingMin());
            return pollingInfo.getStartDateTimePolling().plus(maxTimePolling, ChronoUnit.MINUTES);
        }
        return pollingInfo.getMaxDateTimePolling();
    }

}
