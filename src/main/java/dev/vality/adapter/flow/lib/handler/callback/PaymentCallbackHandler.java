package dev.vality.adapter.flow.lib.handler.callback;

import dev.vality.adapter.flow.lib.model.TemporaryContext;
import dev.vality.adapter.flow.lib.serde.Deserializer;
import dev.vality.adapter.flow.lib.serde.StateSerializer;
import dev.vality.adapter.flow.lib.service.TemporaryContextService;
import dev.vality.damsel.proxy_provider.PaymentCallbackProxyResult;
import dev.vality.damsel.proxy_provider.PaymentCallbackResult;
import dev.vality.damsel.proxy_provider.PaymentContext;
import dev.vality.java.damsel.utils.creators.ProxyProviderPackageCreators;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;

@Slf4j
@RequiredArgsConstructor
public class PaymentCallbackHandler implements CallbackHandler<PaymentCallbackResult, PaymentContext> {

    private final Deserializer<TemporaryContext> temporaryContextDeserializer;
    private final StateSerializer<TemporaryContext> adapterSerializer;
    private final TemporaryContextService temporaryContextService;

    public PaymentCallbackResult handleCallback(ByteBuffer callback, PaymentContext context) {
        TemporaryContext generalExitStateModel = initAdapterContext(callback, context);
        byte[] callbackResponse = new byte[0];
        return ProxyProviderPackageCreators.createCallbackResult(callbackResponse, (new PaymentCallbackProxyResult())
                .setIntent(ProxyProviderPackageCreators.createIntentWithSleepIntent(0))
                .setNextState(this.adapterSerializer.writeByte(generalExitStateModel)));
    }

    private TemporaryContext initAdapterContext(ByteBuffer callback, PaymentContext context) {
        TemporaryContext temporaryContext =
                temporaryContextService.getTemporaryContext(context, this.temporaryContextDeserializer);
        return temporaryContextService.appendThreeDsParametersToContext(callback, temporaryContext);
    }

}