package dev.vality.adapter.flow.lib.handler.callback;

import dev.vality.adapter.common.handler.callback.CallbackHandler;
import dev.vality.adapter.common.state.deserializer.Deserializer;
import dev.vality.adapter.common.state.serializer.StateSerializer;
import dev.vality.adapter.flow.lib.model.TemporaryContext;
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

    private final Deserializer<TemporaryContext> adapterDeserializer;
    private final StateSerializer<TemporaryContext> adapterSerializer;
    private final ParametersDeserializer parametersDeserializer;

    public RecurrentTokenCallbackResult handleCallback(ByteBuffer callback, RecurrentTokenContext context) {
        TemporaryContext generalExitStateModel = initAdapterContext(callback, context);
        byte[] callbackResponse = new byte[0];
        return ProxyProviderPackageCreators.createRecurrentTokenCallbackResult(callbackResponse,
                (new RecurrentTokenProxyResult()).setIntent(RecurrentTokenIntent
                                .sleep(ProxyProviderPackageCreators.createSleepIntent(
                                        BasePackageCreators.createTimerTimeout(0))))
                        .setNextState(this.adapterSerializer.writeByte(generalExitStateModel))
        );
    }

    private TemporaryContext initAdapterContext(ByteBuffer callback, RecurrentTokenContext context) {
        TemporaryContext temporaryContext = AdapterStateUtils.getTemporaryContext(context, this.adapterDeserializer);
        Map<String, String> parameters = parametersDeserializer.read(callback.array());
        if (temporaryContext != null) {
            temporaryContext.setThreeDsData(parameters);
        } else {
            if (parameters == null || parameters.isEmpty()) {
                throw new RuntimeException("Unknown parameters or baseModel!");
            }
        }
        log.info("AdapterContext: {} after callback.", temporaryContext);
        return temporaryContext;
    }

}