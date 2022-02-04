package dev.vality.adapter.flow.lib.converter.exit;

import dev.vality.adapter.common.properties.CommonTimerProperties;
import dev.vality.adapter.common.state.serializer.AdapterSerializer;
import dev.vality.adapter.flow.lib.model.GeneralExitStateModel;
import dev.vality.damsel.proxy_provider.RecurrentTokenProxyResult;
import dev.vality.error.mapping.ErrorMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExitModelToRecTokenProxyResultConverter
        implements Converter<GeneralExitStateModel, RecurrentTokenProxyResult> {

    private final ErrorMapping errorMapping;
    private final CommonTimerProperties timerProperties;
    private final AdapterSerializer serializer;

    @Override
    public RecurrentTokenProxyResult convert(GeneralExitStateModel exitStateModel) {
//
//        if (exitStateModel.getErrorCode() != null) {
//            return createRecurrentTokenProxyResultFailure(
//                    errorMapping.mapFailure(exitStateModel.getErrorCode(), exitStateModel.getErrorMessage()));
//        }
//
//        AdapterContext adapterContext = exitStateModel.getAdapterContext();
//        Step nextStep = adapterContext.getStep();
//        RecurrentTokenIntent intent = switch (nextStep) {
//            case GENERATE_TOKEN_AUTH, GENERATE_TOKEN_REFUND -> RecurrentTokenIntent.sleep(
//                    createSleepIntent(Timer.timeout(0)));
//            case GENERATE_TOKEN_FINISH_THREE_DS, GENERATE_TOKEN_FINISH_THREE_DS_V2 -> createIntentWithSuspendIntent(
//                    exitStateModel);
//            case DO_NOTHING -> RecurrentTokenIntent.finish(
//                    createRecurrentTokenStatusSuccess(exitStateModel.getRecToken())
//            );
//            default -> throw new IllegalStateException("Wrong state: " + nextStep);
//        };
//
//        byte[] nextState = serializer.writeByte(adapterContext);
//        Map<String, String> trxExtra = exitStateModel.getTrxExtra();
//        return new RecurrentTokenProxyResult(intent)
//                .setNextState(nextState)
//                .setTrx(createTransactionInfo(adapterContext.getTrxId(), trxExtra));

        return null;
    }
//
//    private RecurrentTokenIntent createIntentWithSuspendIntent(GeneralExitStateModel exitStateModel) {
//        GeneralEntryStateModel entryStateModel = exitStateModel.getGeneralEntryStateModel();
//        ThreeDsData threeDsData = exitStateModel.getThreeDsData();
//        Map<String, String> params = new HashMap<>(threeDsData.getParameters());
//        params.put(TERM_URL.getValue(), exitStateModel.getGeneralEntryStateModel().getCallbackUrl());
//        int timerRedirectTimeout = extractRedirectTimeout(
//                entryStateModel.getOptions(),
//                timerProperties.getRedirectTimeout());
//        return RecurrentTokenIntent.suspend(
//                createSuspendIntent(
//                        threeDsData.getUniqRedirectOperationIdName(),
//                        timerRedirectTimeout,
//                        createPostUserInteraction(threeDsData.getAcsUrl(), params)));
//    }

}

