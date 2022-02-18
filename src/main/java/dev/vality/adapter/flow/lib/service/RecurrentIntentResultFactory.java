package dev.vality.adapter.flow.lib.service;

import dev.vality.adapter.flow.lib.constant.RedirectFields;
import dev.vality.adapter.flow.lib.model.EntryStateModel;
import dev.vality.adapter.flow.lib.model.ExitStateModel;
import dev.vality.adapter.flow.lib.model.PollingInfo;
import dev.vality.adapter.flow.lib.model.ThreeDsData;
import dev.vality.adapter.flow.lib.utils.CallbackUrlExtractor;
import dev.vality.adapter.flow.lib.utils.ThreeDsDataInitializer;
import dev.vality.adapter.flow.lib.utils.TimerProperties;
import dev.vality.damsel.base.Timer;
import dev.vality.damsel.proxy_provider.*;
import dev.vality.error.mapping.ErrorMapping;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import static dev.vality.java.damsel.utils.creators.ProxyProviderPackageCreators.createPostUserInteraction;
import static dev.vality.java.damsel.utils.creators.ProxyProviderPackageCreators.createRecurrentTokenStatusSuccess;
import static dev.vality.java.damsel.utils.extractors.OptionsExtractors.extractRedirectTimeout;

@RequiredArgsConstructor
public class RecurrentIntentResultFactory {

    private final TimerProperties timerProperties;
    private final CallbackUrlExtractor callbackUrlExtractor;
    private final TagManagementService tagManagementService;
    private final PollingInfoService pollingInfoService;
    private final ErrorMapping errorMapping;
    private final ExponentialBackOffPollingService exponentialBackOffPollingService;

    public RecurrentTokenIntent createIntentWithSuspension(ExitStateModel exitStateModel) {
        EntryStateModel entryStateModel = exitStateModel.getEntryStateModel();
        ThreeDsData threeDsData = exitStateModel.getThreeDsData();
        Map<String, String> params = ThreeDsDataInitializer.initThreeDsData(exitStateModel);
        String redirectUrl = entryStateModel.getBaseRequestModel().getSuccessRedirectUrl();
        params.put(RedirectFields.TERM_URL.getValue(), callbackUrlExtractor.extractCallbackUrl(redirectUrl));
        int timerRedirectTimeout = extractRedirectTimeout(
                entryStateModel.getBaseRequestModel().getAdapterConfigurations(),
                timerProperties.getRedirectTimeoutMin());
        return RecurrentTokenIntent.suspend(
                new SuspendIntent(
                        tagManagementService.findTag(threeDsData.getParameters()),
                        Timer.timeout(timerRedirectTimeout)
                ).setUserInteraction(createPostUserInteraction(threeDsData.getAcsUrl(), params))
        );
    }

    public RecurrentTokenIntent createSleepIntentForReinvocation() {
        return RecurrentTokenIntent.sleep(new SleepIntent(Timer.timeout(0)));
    }

    public RecurrentTokenIntent createSleepIntentWithExponentialPolling(ExitStateModel exitStateModel) {
        EntryStateModel entryStateModel = exitStateModel.getEntryStateModel();
        PollingInfo pollingInfo = pollingInfoService.initPollingInfo(entryStateModel);
        if (pollingInfoService.isDeadline(pollingInfo)) {
            return createFinishIntentFailed("Sleep timeout", "Max time pool limit reached");
        }
        exitStateModel.setPollingInfo(pollingInfo);

        Map<String, String> adapterConfigurations = entryStateModel.getBaseRequestModel().getAdapterConfigurations();
        int nextTimeout =
                exponentialBackOffPollingService.prepareNextPollingInterval(pollingInfo, adapterConfigurations);
        return RecurrentTokenIntent.sleep(
                new SleepIntent(Timer.timeout(nextTimeout))
        );
    }

    public RecurrentTokenIntent createFinishIntent(String recToken) {
        return RecurrentTokenIntent.finish(
                createRecurrentTokenStatusSuccess(recToken)
        );
    }

    public RecurrentTokenIntent createFinishIntentFailed(String errorCode, String errorMessage) {
        return RecurrentTokenIntent.finish(new RecurrentTokenFinishIntent(RecurrentTokenFinishStatus.failure(
                errorMapping.mapFailure(errorCode, errorMessage)))
        );
    }
}
