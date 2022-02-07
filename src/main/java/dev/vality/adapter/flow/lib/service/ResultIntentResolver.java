package dev.vality.adapter.flow.lib.service;

import dev.vality.adapter.common.properties.CommonTimerProperties;
import dev.vality.adapter.flow.lib.constant.Step;
import dev.vality.adapter.flow.lib.model.GeneralEntryStateModel;
import dev.vality.adapter.flow.lib.model.GeneralExitStateModel;
import dev.vality.adapter.flow.lib.model.ThreeDsData;
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
public class ResultIntentResolver {

    private final CommonTimerProperties timerProperties;
    private final CallbackUrlExtractor callbackUrlExtractor;

    public Intent initIntentByStep(GeneralExitStateModel exitStateModel) {
        Step nextStep = exitStateModel.getNextStep();
        GeneralEntryStateModel entryStateModel = exitStateModel.getGeneralEntryStateModel();
        Step currentStep = entryStateModel.getCurrentStep();
        return switch (nextStep) {
            case AUTH -> createIntentWithSleepIntent(0);
            case FINISH_THREE_DS_V1, FINISH_THREE_DS_V2 -> createIntentWithSuspendIntent(exitStateModel);
            case DO_NOTHING -> switch (currentStep) {
                case DO_NOTHING, PAY, AUTH, CAPTURE -> initFinishIntent(exitStateModel, entryStateModel);
                case REFUND, CANCEL -> createFinishIntentSuccess();
                default -> throw new IllegalStateException("Wrong currentStep: " + currentStep);
            };
            case CAPTURE, REFUND, CANCEL -> createFinishIntentSuccess();
            default -> throw new IllegalStateException("Wrong nextStep: " + nextStep);
        };
    }

    private Intent initFinishIntent(GeneralExitStateModel exitStateModel,
                                    GeneralEntryStateModel entryStateModel) {
        if (entryStateModel.getBaseRequestModel().getRecurrentPaymentData() != null
                && entryStateModel.getBaseRequestModel().getRecurrentPaymentData().isMakeRecurrent()) {
            return createFinishIntentSuccessWithToken(exitStateModel.getRecToken());
        }
        return createFinishIntentSuccess();
    }

    private Intent createIntentWithSuspendIntent(GeneralExitStateModel exitStateModel) {
        GeneralEntryStateModel entryStateModel = exitStateModel.getGeneralEntryStateModel();
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
                threeDsData.getUniqRedirectOperationIdName(),
                timerRedirectTimeout,
                createPostUserInteraction(threeDsData.getAcsUrl(), params));
    }

}
