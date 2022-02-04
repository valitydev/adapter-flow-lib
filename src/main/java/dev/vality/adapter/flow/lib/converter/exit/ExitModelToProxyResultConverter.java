package dev.vality.adapter.flow.lib.converter.exit;

import dev.vality.adapter.common.properties.CommonTimerProperties;
import dev.vality.adapter.common.state.serializer.AdapterSerializer;
import dev.vality.adapter.flow.lib.constant.Step;
import dev.vality.adapter.flow.lib.model.GeneralEntryStateModel;
import dev.vality.adapter.flow.lib.model.GeneralExitStateModel;
import dev.vality.adapter.flow.lib.model.ThreeDsData;
import dev.vality.adapter.flow.lib.utils.CallbackUrlExtractor;
import dev.vality.damsel.proxy_provider.Intent;
import dev.vality.damsel.proxy_provider.PaymentProxyResult;
import dev.vality.error.mapping.ErrorMapping;
import dev.vality.java.damsel.utils.creators.DomainPackageCreators;
import dev.vality.java.damsel.utils.creators.ProxyProviderPackageCreators;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

import static dev.vality.adapter.common.constants.ThreeDsFields.TERM_URL;
import static dev.vality.java.damsel.utils.creators.ProxyProviderPackageCreators.*;
import static dev.vality.java.damsel.utils.extractors.OptionsExtractors.extractRedirectTimeout;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExitModelToProxyResultConverter implements Converter<GeneralExitStateModel, PaymentProxyResult> {

    private final ErrorMapping errorMapping;
    private final CommonTimerProperties timerProperties;
    private final AdapterSerializer adapterSerializer;
    private final CallbackUrlExtractor callbackUrlExtractor;

    @Override
    public PaymentProxyResult convert(GeneralExitStateModel exitStateModel) {

        //---error---
        if (StringUtils.hasText(exitStateModel.getErrorCode())) {
            return createProxyResultFailure(
                    errorMapping.mapFailure(exitStateModel.getErrorCode(), exitStateModel.getErrorMessage()));
        }

        Step nextStep = exitStateModel.getNextStep();
        GeneralEntryStateModel entryStateModel = exitStateModel.getGeneralEntryStateModel();
        Intent intent = switch (nextStep) {
            case AUTH -> createIntentWithSleepIntent(0);
            case FINISH_THREE_DS_V1, FINISH_THREE_DS_V2 -> createIntentWithSuspendIntent(exitStateModel);
            case DO_NOTHING -> initFinishIntent(exitStateModel, entryStateModel);
            case CAPTURE, REFUND, CANCEL -> createFinishIntentSuccess();
            default -> throw new IllegalStateException("Wrong state: " + nextStep);
        };

        byte[] nextState = adapterSerializer.writeByte(exitStateModel);
        Map<String, String> trxExtra = exitStateModel.getTrxExtra();

        return new PaymentProxyResult(intent)
                .setNextState(nextState)
                .setTrx(DomainPackageCreators.createTransactionInfo(
                        exitStateModel.getProviderTrxId(), trxExtra)
                );
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

