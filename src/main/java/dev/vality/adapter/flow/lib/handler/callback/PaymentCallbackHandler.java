package dev.vality.adapter.flow.lib.handler.callback;

import dev.vality.adapter.common.handler.callback.CallbackHandler;
import dev.vality.adapter.common.state.deserializer.Deserializer;
import dev.vality.adapter.common.state.serializer.StateSerializer;
import dev.vality.adapter.flow.lib.model.GeneralExitStateModel;
import dev.vality.adapter.flow.lib.utils.AdapterStateUtils;
import dev.vality.adapter.flow.lib.utils.ParametersDeserializer;
import dev.vality.damsel.proxy_provider.PaymentCallbackProxyResult;
import dev.vality.damsel.proxy_provider.PaymentCallbackResult;
import dev.vality.damsel.proxy_provider.PaymentContext;
import dev.vality.java.damsel.utils.creators.ProxyProviderPackageCreators;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class PaymentCallbackHandler implements CallbackHandler<PaymentCallbackResult, PaymentContext> {

    private final Deserializer<GeneralExitStateModel> adapterDeserializer;
    private final StateSerializer<GeneralExitStateModel> adapterSerializer;
    private final ParametersDeserializer parametersDeserializer;

    public PaymentCallbackResult handleCallback(ByteBuffer callback, PaymentContext context) {
        GeneralExitStateModel generalExitStateModel = initAdapterContext(callback, context);
        byte[] callbackResponse = new byte[0];
        return ProxyProviderPackageCreators.createCallbackResult(callbackResponse, (new PaymentCallbackProxyResult())
                .setIntent(ProxyProviderPackageCreators.createIntentWithSleepIntent(0))
                .setNextState(this.adapterSerializer.writeByte(generalExitStateModel)));
    }

    private GeneralExitStateModel initAdapterContext(ByteBuffer callback, PaymentContext context) {
        GeneralExitStateModel generalExitStateModel =
                AdapterStateUtils.getGeneralExitStateModel(context, this.adapterDeserializer);
        Map<String, String> parameters = parametersDeserializer.read(callback.array());
        if (generalExitStateModel != null
                && generalExitStateModel.getGeneralEntryStateModel() != null
                && generalExitStateModel.getGeneralEntryStateModel().getBaseRequestModel() != null) {
            generalExitStateModel.getGeneralEntryStateModel().getBaseRequestModel().setThreeDsData(parameters);
        } else {
            if (parameters == null || parameters.isEmpty()) {
                throw new RuntimeException("Unknown parameters or baseModel!");
            }
        }
        log.info("AdapterContext: {} after callback.", generalExitStateModel);
        return generalExitStateModel;
    }

}