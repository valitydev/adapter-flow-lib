package dev.vality.adapter.flow.lib.handler.callback;

import dev.vality.adapter.common.handler.callback.CallbackHandler;
import dev.vality.adapter.flow.lib.model.GeneralExitStateModel;
import dev.vality.adapter.flow.lib.utils.AdapterDeserializer;
import dev.vality.adapter.flow.lib.utils.AdapterSerializer;
import dev.vality.adapter.flow.lib.utils.AdapterStateUtils;
import dev.vality.adapter.flow.lib.utils.ParametersDeserializer;
import dev.vality.damsel.proxy_provider.RecurrentTokenCallbackResult;
import dev.vality.damsel.proxy_provider.RecurrentTokenContext;
import dev.vality.damsel.proxy_provider.RecurrentTokenIntent;
import dev.vality.damsel.proxy_provider.RecurrentTokenProxyResult;
import dev.vality.java.damsel.utils.creators.BasePackageCreators;
import dev.vality.java.damsel.utils.creators.ProxyProviderPackageCreators;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class RecurrentTokenCallbackHandler
        implements CallbackHandler<RecurrentTokenCallbackResult, RecurrentTokenContext> {

    private final AdapterDeserializer adapterDeserializer;
    private final AdapterSerializer adapterSerializer;
    private final ParametersDeserializer parametersDeserializer;

    public RecurrentTokenCallbackResult handleCallback(ByteBuffer callback, RecurrentTokenContext context) {
        GeneralExitStateModel generalExitStateModel = initAdapterContext(callback, context);
        byte[] callbackResponse = new byte[0];
        return ProxyProviderPackageCreators.createRecurrentTokenCallbackResult(callbackResponse,
                (new RecurrentTokenProxyResult()).setIntent(RecurrentTokenIntent
                                .sleep(ProxyProviderPackageCreators.createSleepIntent(
                                        BasePackageCreators.createTimerTimeout(0))))
                        .setNextState(this.adapterSerializer.writeByte(generalExitStateModel))
        );
    }

    private GeneralExitStateModel initAdapterContext(ByteBuffer callback, RecurrentTokenContext context) {
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