package dev.vality.adapter.flow.lib.service.factory;

import dev.vality.adapter.common.mapper.ErrorMapping;
import dev.vality.adapter.flow.lib.constant.HttpMethod;
import dev.vality.adapter.flow.lib.constant.RedirectFields;
import dev.vality.adapter.flow.lib.model.EntryStateModel;
import dev.vality.adapter.flow.lib.model.ExitStateModel;
import dev.vality.adapter.flow.lib.model.PollingInfo;
import dev.vality.adapter.flow.lib.model.ThreeDsData;
import dev.vality.adapter.flow.lib.serde.ParametersSerializer;
import dev.vality.adapter.flow.lib.service.CallbackUrlExtractor;
import dev.vality.adapter.flow.lib.service.ExponentialBackOffPollingService;
import dev.vality.adapter.flow.lib.service.PollingInfoService;
import dev.vality.adapter.flow.lib.service.TagManagementService;
import dev.vality.adapter.flow.lib.utils.ThreeDsDataInitializer;
import dev.vality.adapter.flow.lib.utils.TimeoutUtils;
import dev.vality.adapter.flow.lib.utils.TimerProperties;
import dev.vality.damsel.base.Timer;
import dev.vality.damsel.proxy_provider.*;
import dev.vality.damsel.timeout_behaviour.TimeoutBehaviour;
import dev.vality.damsel.user_interaction.UserInteraction;
import lombok.RequiredArgsConstructor;
import org.springframework.util.ObjectUtils;

import java.nio.ByteBuffer;
import java.util.Map;

import static dev.vality.adapter.common.damsel.OptionsExtractors.extractRedirectTimeout;
import static dev.vality.adapter.common.damsel.ProxyProviderPackageCreators.*;

@RequiredArgsConstructor
public class SimpleIntentResultFactory implements IntentResultFactory {

    private final TimerProperties timerProperties;
    private final CallbackUrlExtractor callbackUrlExtractor;
    private final TagManagementService tagManagementService;
    private final ParametersSerializer parametersSerializer;
    private final PollingInfoService pollingInfoService;
    private final ErrorMapping errorMapping;
    private final ExponentialBackOffPollingService exponentialBackOffPollingService;

    @Override
    public Intent createFinishIntentSuccessWithCheckToken(ExitStateModel exitStateModel) {
        EntryStateModel entryStateModel = exitStateModel.getEntryStateModel();
        if (entryStateModel.getBaseRequestModel().getRecurrentPaymentData() != null
                && entryStateModel.getBaseRequestModel().getRecurrentPaymentData().isMakeRecurrent()) {
            return createFinishIntentSuccessWithToken(exitStateModel.getRecToken());
        }
        return createFinishIntentSuccess(exitStateModel);
    }

    @Override
    public Intent createSuspendIntentWithFailedAfterTimeout(ExitStateModel exitStateModel) {
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
        return Intent.suspend(
                new SuspendIntent(
                        tagManagementService.findTag(params),
                        Timer.timeout(TimeoutUtils.toSeconds(timerRedirectTimeoutMin))
                ).setUserInteraction(createUserInteraction(threeDsData, params))
        );
    }

    @Override
    public Intent createSuspendIntentWithCallbackAfterTimeout(ExitStateModel exitStateModel) {
        Map<String, String> params = ThreeDsDataInitializer.initThreeDsParameters(exitStateModel);
        EntryStateModel entryStateModel = exitStateModel.getEntryStateModel();

        PollingInfo pollingInfo = pollingInfoService.initPollingInfo(entryStateModel);
        if (pollingInfoService.isDeadline(pollingInfo)) {
            return createFinishIntentFailed("Sleep timeout", "Max time pool limit reached");
        }
        exitStateModel.setPollingInfo(pollingInfo);

        String redirectUrl = entryStateModel.getBaseRequestModel().getSuccessRedirectUrl();
        Map<String, String> adapterConfigurations = entryStateModel.getBaseRequestModel().getAdapterConfigurations();
        params.put(RedirectFields.TERM_URL.getValue(),
                callbackUrlExtractor.extractCallbackUrl(adapterConfigurations, redirectUrl));
        ThreeDsData threeDsData = exitStateModel.getThreeDsData();
        int timerRedirectTimeoutMin = extractRedirectTimeout(
                adapterConfigurations,
                timerProperties.getRedirectTimeoutMin());
        return Intent.suspend(
                new SuspendIntent(
                        tagManagementService.findTag(params),
                        Timer.timeout(TimeoutUtils.toSeconds(timerRedirectTimeoutMin)))
                        .setTimeoutBehaviour(TimeoutBehaviour.callback(
                                ByteBuffer.wrap(parametersSerializer.writeByte(params)))
                        ).setUserInteraction(createUserInteraction(threeDsData, params))
        );
    }

    private UserInteraction createUserInteraction(ThreeDsData threeDsData, Map<String, String> parameters) {
        if (threeDsData.getHttpMethod() == HttpMethod.GET) {
            return createGetUserInteraction(threeDsData.getAcsUrl());
        } else {
            return createPostUserInteraction(threeDsData.getAcsUrl(), parameters);
        }
    }

    @Override
    public Intent createFinishIntentSuccess(ExitStateModel exitStateModel) {
        var success = new Success();
        if (!ObjectUtils.isEmpty(exitStateModel.getChangedCost())) {
            success.setChangedCost(exitStateModel.getChangedCost());
        }
        return Intent.finish(new FinishIntent(FinishStatus.success(success)));
    }

    @Override
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

    @Override
    public Intent createFinishIntentFailed(ExitStateModel exitStateModel) {
        return Intent.finish(new FinishIntent(FinishStatus.failure(
                errorMapping.mapFailure(exitStateModel.getErrorCode(),
                        exitStateModel.getErrorMessage()))));
    }

    @Override
    public Intent createFinishIntentFailed(String errorCode, String errorMessage) {
        return Intent.finish(new FinishIntent(FinishStatus.failure(
                errorMapping.mapFailure(errorCode, errorMessage))));
    }

}
