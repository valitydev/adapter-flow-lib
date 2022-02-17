package dev.vality.adapter.flow.lib.service;

import dev.vality.adapter.flow.lib.constant.RedirectFields;
import dev.vality.adapter.flow.lib.model.EntryStateModel;
import dev.vality.adapter.flow.lib.model.ExitStateModel;
import dev.vality.adapter.flow.lib.model.ThreeDsData;
import dev.vality.adapter.flow.lib.utils.CallbackUrlExtractor;
import dev.vality.adapter.flow.lib.utils.TimerProperties;
import dev.vality.damsel.base.Timer;
import dev.vality.damsel.proxy_provider.*;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import static dev.vality.java.damsel.utils.creators.ProxyProviderPackageCreators.createFinishIntentSuccessWithToken;
import static dev.vality.java.damsel.utils.creators.ProxyProviderPackageCreators.createPostUserInteraction;
import static dev.vality.java.damsel.utils.extractors.OptionsExtractors.extractRedirectTimeout;

@RequiredArgsConstructor
public class IntentResultFactory {

    private final TimerProperties timerProperties;
    private final CallbackUrlExtractor callbackUrlExtractor;
    private final TagManagementService tagManagementService;

    public Intent createFinishIntentWithCheckToken(ExitStateModel exitStateModel) {
        EntryStateModel entryStateModel = exitStateModel.getEntryStateModel();
        if (entryStateModel.getBaseRequestModel().getRecurrentPaymentData() != null
                && entryStateModel.getBaseRequestModel().getRecurrentPaymentData().isMakeRecurrent()) {
            return createFinishIntentSuccessWithToken(exitStateModel.getRecToken());
        }
        return createFinishIntentSuccess();
    }

    public Intent createIntentWithSuspension(ExitStateModel exitStateModel) {
        EntryStateModel entryStateModel = exitStateModel.getEntryStateModel();
        ThreeDsData threeDsData = exitStateModel.getThreeDsData();
        Map<String, String> params = new HashMap<>();
        String tag = exitStateModel.getProviderTrxId();
        if (threeDsData.getParameters() != null) {
            params.putAll(threeDsData.getParameters());
            tag = tagManagementService.findTag(threeDsData.getParameters());
        }
        String redirectUrl = entryStateModel.getBaseRequestModel().getSuccessRedirectUrl();
        params.put(RedirectFields.TERM_URL.getValue(), callbackUrlExtractor.extractCallbackUrl(redirectUrl));
        int timerRedirectTimeout = extractRedirectTimeout(
                entryStateModel.getBaseRequestModel().getAdapterConfigurations(),
                timerProperties.getRedirectTimeout());
        return Intent.suspend(
                new SuspendIntent(
                        tag,
                        Timer.timeout(timerRedirectTimeout)
                ).setUserInteraction(createPostUserInteraction(threeDsData.getAcsUrl(), params))
        );
    }

    public Intent createFinishIntentSuccess() {
        return Intent.finish(new FinishIntent(FinishStatus.success(new Success())));
    }

    public Intent createSleepIntent() {
        return Intent.sleep(new SleepIntent(Timer.timeout(0)));
    }

}
