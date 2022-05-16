package dev.vality.adapter.flow.lib.handler.callback;

import dev.vality.adapter.common.damsel.BasePackageCreators;
import dev.vality.adapter.common.damsel.ProxyProviderPackageCreators;
import dev.vality.adapter.flow.lib.model.TemporaryContext;
import dev.vality.adapter.flow.lib.serde.Deserializer;
import dev.vality.adapter.flow.lib.serde.StateSerializer;
import dev.vality.adapter.flow.lib.service.TemporaryContextService;
import dev.vality.damsel.proxy_provider.RecurrentTokenCallbackResult;
import dev.vality.damsel.proxy_provider.RecurrentTokenContext;
import dev.vality.damsel.proxy_provider.RecurrentTokenIntent;
import dev.vality.damsel.proxy_provider.RecurrentTokenProxyResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;

@Slf4j
@RequiredArgsConstructor
public class RecurrentTokenCallbackHandler
        implements CallbackHandler<RecurrentTokenCallbackResult, RecurrentTokenContext> {

    private final Deserializer<TemporaryContext> adapterDeserializer;
    private final StateSerializer<TemporaryContext> adapterSerializer;
    private final TemporaryContextService temporaryContextService;

    public RecurrentTokenCallbackResult handleCallback(ByteBuffer callback, RecurrentTokenContext context) {
        var generalExitStateModel = initTemporaryContext(callback, context);
        byte[] callbackResponse = new byte[0];
        return ProxyProviderPackageCreators.createRecurrentTokenCallbackResult(callbackResponse,
                (new RecurrentTokenProxyResult()).setIntent(RecurrentTokenIntent
                                .sleep(ProxyProviderPackageCreators.createSleepIntent(
                                        BasePackageCreators.createTimerTimeout(0))))
                        .setNextState(this.adapterSerializer.writeByte(generalExitStateModel))
        );
    }

    private TemporaryContext initTemporaryContext(ByteBuffer callback, RecurrentTokenContext context) {
        var temporaryContext = temporaryContextService.getTemporaryContext(context, this.adapterDeserializer);
        return temporaryContextService.appendThreeDsParametersToContext(callback, temporaryContext);
    }

}