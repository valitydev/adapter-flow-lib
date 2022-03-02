package dev.vality.adapter.flow.lib.service;

import dev.vality.adapter.flow.lib.constant.RedirectFields;
import dev.vality.adapter.flow.lib.model.EntryStateModel;
import dev.vality.adapter.flow.lib.model.ExitStateModel;
import dev.vality.adapter.flow.lib.model.PollingInfo;
import dev.vality.adapter.flow.lib.model.ThreeDsData;
import dev.vality.adapter.flow.lib.serde.ParametersSerializer;
import dev.vality.adapter.flow.lib.utils.CallbackUrlExtractor;
import dev.vality.adapter.flow.lib.utils.ThreeDsDataInitializer;
import dev.vality.adapter.flow.lib.utils.TimeoutUtils;
import dev.vality.adapter.flow.lib.utils.TimerProperties;
import dev.vality.damsel.base.Timer;
import dev.vality.damsel.proxy_provider.*;
import dev.vality.damsel.timeout_behaviour.TimeoutBehaviour;
import dev.vality.error.mapping.ErrorMapping;
import lombok.RequiredArgsConstructor;

import java.nio.ByteBuffer;
import java.util.Map;

import static dev.vality.java.damsel.utils.creators.ProxyProviderPackageCreators.*;
import static dev.vality.java.damsel.utils.extractors.OptionsExtractors.extractRedirectTimeout;

@RequiredArgsConstructor
public class IntentResultFactory {

    private final TimerProperties timerProperties;
    private final CallbackUrlExtractor callbackUrlExtractor;
    private final TagManagementService tagManagementService;
    private final ParametersSerializer parametersSerializer;
    private final PollingInfoService pollingInfoService;
    private final ErrorMapping errorMapping;
    private final ExponentialBackOffPollingService exponentialBackOffPollingService;

    public Intent createFinishIntentSuccessWithCheckToken(ExitStateModel exitStateModel) {
        EntryStateModel entryStateModel = exitStateModel.getEntryStateModel();
        if (entryStateModel.getBaseRequestModel().getRecurrentPaymentData() != null
                && entryStateModel.getBaseRequestModel().getRecurrentPaymentData().isMakeRecurrent()) {
            return createFinishIntentSuccessWithToken(exitStateModel.getRecToken());
        }
        return createFinishIntentSuccess();
    }

    public Intent createSuspendIntentWithFailedAfterTimeout(ExitStateModel exitStateModel) {
        EntryStateModel entryStateModel = exitStateModel.getEntryStateModel();
        ThreeDsData threeDsData = exitStateModel.getThreeDsData();
        Map<String, String> params = ThreeDsDataInitializer.initThreeDsParameters(exitStateModel);
        String redirectUrl = entryStateModel.getBaseRequestModel().getSuccessRedirectUrl();
        params.put(RedirectFields.TERM_URL.getValue(), callbackUrlExtractor.extractCallbackUrl(redirectUrl));
        int timerRedirectTimeout = extractRedirectTimeout(
                entryStateModel.getBaseRequestModel().getAdapterConfigurations(),
                timerProperties.getRedirectTimeoutMin());
        return Intent.suspend(
                new SuspendIntent(
                        tagManagementService.findTag(params),
                        Timer.timeout(timerRedirectTimeout)
                ).setUserInteraction(createPostUserInteraction(threeDsData.getAcsUrl(), params))
        );
    }

    public Intent createSuspendIntentWithCallbackAfterTimeout(ExitStateModel exitStateModel) {
        Map<String, String> params = ThreeDsDataInitializer.initThreeDsParameters(exitStateModel);
        EntryStateModel entryStateModel = exitStateModel.getEntryStateModel();

        PollingInfo pollingInfo = pollingInfoService.initPollingInfo(entryStateModel);
        if (pollingInfoService.isDeadline(pollingInfo)) {
            return createFinishIntentFailed("Sleep timeout", "Max time pool limit reached");
        }
        exitStateModel.setPollingInfo(pollingInfo);

        String redirectUrl = entryStateModel.getBaseRequestModel().getSuccessRedirectUrl();
        params.put(RedirectFields.TERM_URL.getValue(), callbackUrlExtractor.extractCallbackUrl(redirectUrl));
        ThreeDsData threeDsData = exitStateModel.getThreeDsData();
        int timerRedirectTimeoutMin = extractRedirectTimeout(
                entryStateModel.getBaseRequestModel().getAdapterConfigurations(),
                timerProperties.getRedirectTimeoutMin());
        return Intent.suspend(
                new SuspendIntent(
                        tagManagementService.findTag(params),
                        Timer.timeout(TimeoutUtils.toSeconds(timerRedirectTimeoutMin)))
                        .setTimeoutBehaviour(TimeoutBehaviour.callback(
                                ByteBuffer.wrap(parametersSerializer.writeByte(params)))
                        ).setUserInteraction(createGetUserInteraction(threeDsData.getAcsUrl()))
        );
    }

    public Intent createFinishIntentSuccess() {
        return Intent.finish(new FinishIntent(FinishStatus.success(new Success())));
    }

    public Intent createSleepIntentForReinvocation() {
        return Intent.sleep(new SleepIntent(Timer.timeout(0)));
    }

    public Intent createSleepIntentWithExponentialPolling(ExitStateModel exitStateModel) {
        EntryStateModel entryStateModel = exitStateModel.getEntryStateModel();
        PollingInfo pollingInfo = pollingInfoService.initPollingInfo(entryStateModel);
        if (pollingInfoService.isDeadline(pollingInfo)) {
            return createFinishIntentFailed("Sleep timeout", "Max time pool limit reached");
        }
        exitStateModel.setPollingInfo(pollingInfo);

        Map<String, String> adapterConfigurations = entryStateModel.getBaseRequestModel().getAdapterConfigurations();
        int nextTimeoutSec =
                exponentialBackOffPollingService.prepareNextPollingInterval(pollingInfo, adapterConfigurations);
        return Intent.sleep(new SleepIntent(Timer.timeout(nextTimeoutSec)));
    }

    public Intent createFinishIntentFailed(ExitStateModel exitStateModel) {
        return Intent.finish(new FinishIntent(FinishStatus.failure(
                errorMapping.mapFailure(exitStateModel.getErrorCode(),
                        exitStateModel.getErrorMessage()))));
    }

    public Intent createFinishIntentFailed(String errorCode, String errorMessage) {
        return Intent.finish(new FinishIntent(FinishStatus.failure(
                errorMapping.mapFailure(errorCode, errorMessage))));
    }

}
