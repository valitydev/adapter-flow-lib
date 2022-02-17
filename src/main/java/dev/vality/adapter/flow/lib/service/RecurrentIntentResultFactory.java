package dev.vality.adapter.flow.lib.service;

import dev.vality.adapter.flow.lib.constant.RedirectFields;
import dev.vality.adapter.flow.lib.model.EntryStateModel;
import dev.vality.adapter.flow.lib.model.ExitStateModel;
import dev.vality.adapter.flow.lib.model.ThreeDsData;
import dev.vality.adapter.flow.lib.utils.CallbackUrlExtractor;
import dev.vality.adapter.flow.lib.utils.TimerProperties;
import dev.vality.damsel.base.Timer;
import dev.vality.damsel.proxy_provider.RecurrentTokenIntent;
import dev.vality.damsel.proxy_provider.SleepIntent;
import dev.vality.damsel.proxy_provider.SuspendIntent;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import static dev.vality.java.damsel.utils.creators.ProxyProviderPackageCreators.createPostUserInteraction;
import static dev.vality.java.damsel.utils.creators.ProxyProviderPackageCreators.createRecurrentTokenStatusSuccess;
import static dev.vality.java.damsel.utils.extractors.OptionsExtractors.extractRedirectTimeout;

@RequiredArgsConstructor
public class RecurrentIntentResultFactory {

    private final TimerProperties timerProperties;
    private final CallbackUrlExtractor callbackUrlExtractor;
    private final TagManagementService tagManagementService;

    public RecurrentTokenIntent createIntentWithSuspension(ExitStateModel exitStateModel) {
        EntryStateModel entryStateModel = exitStateModel.getGeneralEntryStateModel();
        ThreeDsData threeDsData = exitStateModel.getThreeDsData();
        Map<String, String> params = new HashMap<>(threeDsData.getParameters());
        String redirectUrl = entryStateModel.getBaseRequestModel().getSuccessRedirectUrl();
        params.put(RedirectFields.TERM_URL.getValue(), callbackUrlExtractor.extractCallbackUrl(redirectUrl));
        int timerRedirectTimeout = extractRedirectTimeout(
                entryStateModel.getBaseRequestModel().getAdapterConfigurations(),
                timerProperties.getRedirectTimeout());
        return RecurrentTokenIntent.suspend(
                new SuspendIntent(
                        tagManagementService.findTag(threeDsData.getParameters()),
                        Timer.timeout(timerRedirectTimeout)
                ).setUserInteraction(createPostUserInteraction(threeDsData.getAcsUrl(), params))
        );
    }

    public RecurrentTokenIntent createSleepIntent() {
        return RecurrentTokenIntent.sleep(new SleepIntent(Timer.timeout(0)));
    }

    public RecurrentTokenIntent createFinishIntent(String recToken) {
        return RecurrentTokenIntent.finish(
                createRecurrentTokenStatusSuccess(recToken)
        );
    }
}
