package dev.vality.adapter.flow.lib.flow.full;

import dev.vality.adapter.common.properties.CommonTimerProperties;
import dev.vality.adapter.flow.lib.constant.Step;
import dev.vality.adapter.flow.lib.flow.RecurrentResultIntentResolver;
import dev.vality.adapter.flow.lib.model.EntryStateModel;
import dev.vality.adapter.flow.lib.model.ExitStateModel;
import dev.vality.adapter.flow.lib.model.ThreeDsData;
import dev.vality.adapter.flow.lib.service.TagManagementService;
import dev.vality.adapter.flow.lib.utils.CallbackUrlExtractor;
import dev.vality.damsel.base.Timer;
import dev.vality.damsel.proxy_provider.RecurrentTokenIntent;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import static dev.vality.adapter.common.constants.ThreeDsFields.TERM_URL;
import static dev.vality.java.damsel.utils.creators.ProxyProviderPackageCreators.*;
import static dev.vality.java.damsel.utils.extractors.OptionsExtractors.extractRedirectTimeout;

@RequiredArgsConstructor
public class RecurrentResultIntentResolverImpl implements RecurrentResultIntentResolver {

    private final CommonTimerProperties timerProperties;
    private final CallbackUrlExtractor callbackUrlExtractor;
    private final TagManagementService tagManagementService;

    @Override
    public RecurrentTokenIntent initIntentByStep(ExitStateModel exitStateModel) {
        Step nextStep = exitStateModel.getNextStep();
        return switch (nextStep) {
            case REFUND, CAPTURE -> RecurrentTokenIntent.sleep(
                    createSleepIntent(Timer.timeout(0)));
            case FINISH_THREE_DS_V1, FINISH_THREE_DS_V2 -> createIntentWithSuspendIntent(
                    exitStateModel);
            case DO_NOTHING -> RecurrentTokenIntent.finish(
                    createRecurrentTokenStatusSuccess(exitStateModel.getRecToken())
            );
            default -> throw new IllegalStateException("Wrong state: " + nextStep);
        };
    }

    private RecurrentTokenIntent createIntentWithSuspendIntent(ExitStateModel exitStateModel) {
        EntryStateModel entryStateModel = exitStateModel.getGeneralEntryStateModel();
        ThreeDsData threeDsData = exitStateModel.getThreeDsData();
        Map<String, String> params = new HashMap<>(threeDsData.getParameters());
        params.put(TERM_URL.getValue(), callbackUrlExtractor.extractCallbackUrl(
                entryStateModel.getBaseRequestModel().getAdapterConfigurations(),
                entryStateModel.getRedirectUrl())
        );
        int timerRedirectTimeout = extractRedirectTimeout(
                entryStateModel.getBaseRequestModel().getAdapterConfigurations(),
                timerProperties.getRedirectTimeout());
        return RecurrentTokenIntent.suspend(
                createSuspendIntent(
                        tagManagementService.findTag(threeDsData.getParameters()),
                        timerRedirectTimeout,
                        createPostUserInteraction(threeDsData.getAcsUrl(), params)));
    }

}