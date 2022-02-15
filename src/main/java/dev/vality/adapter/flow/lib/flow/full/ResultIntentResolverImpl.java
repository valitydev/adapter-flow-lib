package dev.vality.adapter.flow.lib.flow.full;

import dev.vality.adapter.common.properties.CommonTimerProperties;
import dev.vality.adapter.flow.lib.constant.Step;
import dev.vality.adapter.flow.lib.flow.ResultIntentResolver;
import dev.vality.adapter.flow.lib.model.EntryStateModel;
import dev.vality.adapter.flow.lib.model.ExitStateModel;
import dev.vality.adapter.flow.lib.model.ThreeDsData;
import dev.vality.adapter.flow.lib.service.TagManagementService;
import dev.vality.adapter.flow.lib.utils.CallbackUrlExtractor;
import dev.vality.damsel.proxy_provider.Intent;
import dev.vality.java.damsel.utils.creators.ProxyProviderPackageCreators;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import static dev.vality.adapter.common.constants.ThreeDsFields.TERM_URL;
import static dev.vality.java.damsel.utils.creators.ProxyProviderPackageCreators.*;
import static dev.vality.java.damsel.utils.extractors.OptionsExtractors.extractRedirectTimeout;

@RequiredArgsConstructor
public class ResultIntentResolverImpl implements ResultIntentResolver {

    private final CommonTimerProperties timerProperties;
    private final CallbackUrlExtractor callbackUrlExtractor;
    private final TagManagementService tagManagementService;

    @Override
    public Intent initIntentByStep(ExitStateModel exitStateModel) {
        Step nextStep = exitStateModel.getNextStep();
        EntryStateModel entryStateModel = exitStateModel.getGeneralEntryStateModel();
        Step currentStep = entryStateModel.getCurrentStep();
        return switch (nextStep) {
            case FINISH_THREE_DS_V1, CHECK_NEED_3DS_V2, FINISH_THREE_DS_V2 -> createIntentWithSuspendIntent(
                    exitStateModel);
            case DO_NOTHING -> switch (currentStep) {
                case CHECK_NEED_3DS_V2, FINISH_THREE_DS_V1, FINISH_THREE_DS_V2,
                        DO_NOTHING, PAY, AUTH, CAPTURE -> initFinishIntent(exitStateModel);
                case REFUND, CANCEL -> createFinishIntentSuccess();
                default -> throw new IllegalStateException("Wrong currentStep: " + currentStep);
            };
            case CAPTURE, REFUND, CANCEL -> createFinishIntentSuccess();
            default -> throw new IllegalStateException("Wrong nextStep: " + nextStep);
        };
    }

    private Intent initFinishIntent(ExitStateModel exitStateModel) {
        EntryStateModel entryStateModel = exitStateModel.getGeneralEntryStateModel();
        if (entryStateModel.getBaseRequestModel().getRecurrentPaymentData() != null
                && entryStateModel.getBaseRequestModel().getRecurrentPaymentData().isMakeRecurrent()) {
            return createFinishIntentSuccessWithToken(exitStateModel.getRecToken());
        }
        return createFinishIntentSuccess();
    }

    private Intent createIntentWithSuspendIntent(ExitStateModel exitStateModel) {
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
        return ProxyProviderPackageCreators.createIntentWithSuspendIntent(
                tagManagementService.findTag(threeDsData.getParameters()),
                timerRedirectTimeout,
                createPostUserInteraction(threeDsData.getAcsUrl(), params));
    }


}
