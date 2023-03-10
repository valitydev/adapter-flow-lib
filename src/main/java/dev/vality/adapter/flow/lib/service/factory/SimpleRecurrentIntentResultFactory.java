package dev.vality.adapter.flow.lib.service.factory;

import dev.vality.adapter.common.mapper.ErrorMapping;
import dev.vality.adapter.flow.lib.constant.RedirectFields;
import dev.vality.adapter.flow.lib.model.EntryStateModel;
import dev.vality.adapter.flow.lib.model.ExitStateModel;
import dev.vality.adapter.flow.lib.model.PollingInfo;
import dev.vality.adapter.flow.lib.model.ThreeDsData;
import dev.vality.adapter.flow.lib.service.CallbackUrlExtractor;
import dev.vality.adapter.flow.lib.service.ExponentialBackOffPollingService;
import dev.vality.adapter.flow.lib.service.PollingInfoService;
import dev.vality.adapter.flow.lib.service.TagManagementService;
import dev.vality.adapter.flow.lib.utils.ThreeDsDataInitializer;
import dev.vality.adapter.flow.lib.utils.TimeoutUtils;
import dev.vality.adapter.flow.lib.utils.TimerProperties;
import dev.vality.damsel.base.Timer;
import dev.vality.damsel.proxy_provider.*;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import static dev.vality.adapter.common.damsel.OptionsExtractors.extractRedirectTimeout;
import static dev.vality.adapter.common.damsel.ProxyProviderPackageCreators.createPostUserInteraction;
import static dev.vality.adapter.common.damsel.ProxyProviderPackageCreators.createRecurrentTokenStatusSuccess;

@RequiredArgsConstructor
public class SimpleRecurrentIntentResultFactory implements RecurrentIntentResultFactory {

    private final TimerProperties timerProperties;
    private final CallbackUrlExtractor callbackUrlExtractor;
    private final TagManagementService tagManagementService;
    private final PollingInfoService pollingInfoService;
    private final ErrorMapping errorMapping;
    private final ExponentialBackOffPollingService exponentialBackOffPollingService;

    @Override
    public RecurrentTokenIntent createIntentWithSuspension(ExitStateModel exitStateModel) {
        EntryStateModel entryStateModel = exitStateModel.getEntryStateModel();
        ThreeDsData threeDsData = exitStateModel.getThreeDsData();
        Map<String, String> params = ThreeDsDataInitializer.initThreeDsParameters(exitStateModel);
        String redirectUrl = entryStateModel.getBaseRequestModel().getSuccessRedirectUrl();
        Map<String, String> adapterConfigurations = entryStateModel.getBaseRequestModel().getAdapterConfigurations();
        params.put(RedirectFields.TERM_URL.getValue(),
                callbackUrlExtractor.extractCallbackUrl(adapterConfigurations, redirectUrl));
        int timerRedirectTimeoutMin = extractRedirectTimeout(
                adapterConfigurations,
                timerProperties.getRedirectTimeoutMin());
        return RecurrentTokenIntent.suspend(
                new SuspendIntent(
                        tagManagementService.findTag(params),
                        Timer.timeout(TimeoutUtils.toSeconds(timerRedirectTimeoutMin))
                ).setUserInteraction(createPostUserInteraction(threeDsData.getAcsUrl(), params))
        );
    }

    @Override
    public RecurrentTokenIntent createSleepIntentForReinvocation() {
        return RecurrentTokenIntent.sleep(new SleepIntent(Timer.timeout(0)));
    }

    @Override
    public RecurrentTokenIntent createSleepIntentWithExponentialPolling(ExitStateModel exitStateModel) {
        EntryStateModel entryStateModel = exitStateModel.getEntryStateModel();
        PollingInfo pollingInfo = pollingInfoService.initPollingInfo(entryStateModel);
        if (pollingInfoService.isDeadline(pollingInfo)) {
            return createFinishIntentFailed("Sleep timeout", "Max time pool limit reached");
        }
        exitStateModel.setPollingInfo(pollingInfo);

        Map<String, String> adapterConfigurations = entryStateModel.getBaseRequestModel().getAdapterConfigurations();
        int nextTimeoutSec =
                exponentialBackOffPollingService.prepareNextPollingInterval(pollingInfo, adapterConfigurations);
        return RecurrentTokenIntent.sleep(
                new SleepIntent(Timer.timeout(nextTimeoutSec))
        );
    }

    @Override
    public RecurrentTokenIntent createFinishIntent(String recToken) {
        return RecurrentTokenIntent.finish(
                createRecurrentTokenStatusSuccess(recToken)
        );
    }

    @Override
    public RecurrentTokenIntent createFinishIntentFailed(String errorCode, String errorMessage) {
        return RecurrentTokenIntent.finish(new RecurrentTokenFinishIntent(RecurrentTokenFinishStatus.failure(
                errorMapping.mapFailure(errorCode, errorMessage)))
        );
    }
}
