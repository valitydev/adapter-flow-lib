package dev.vality.adapter.flow.lib.handler.callback;

import dev.vality.adapter.common.enums.Step;
import dev.vality.adapter.common.handler.callback.CallbackHandler;
import dev.vality.adapter.common.model.AdapterContext;
import dev.vality.adapter.common.model.Callback;
import dev.vality.adapter.common.state.deserializer.AdapterDeserializer;
import dev.vality.adapter.common.state.deserializer.CallbackDeserializer;
import dev.vality.adapter.common.state.serializer.AdapterSerializer;
import dev.vality.adapter.common.state.utils.AdapterStateUtils;
import dev.vality.adapter.flow.lib.model.ThreeDsV2Callback;
import dev.vality.adapter.flow.lib.utils.ThreeDsV2CallbackDeserializer;
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

    private final AdapterDeserializer adapterDeserializer;
    private final AdapterSerializer adapterSerializer;
    private final CallbackDeserializer callbackDeserializer;
    private final ThreeDsV2CallbackDeserializer threeDsV2CallbackDeserializer;

    public PaymentCallbackResult handleCallback(ByteBuffer callback, PaymentContext context) {
        AdapterContext adapterContext = initAdapterContext(callback, context);
        byte[] callbackResponse = new byte[0];
        return ProxyProviderPackageCreators.createCallbackResult(callbackResponse, (new PaymentCallbackProxyResult())
                .setIntent(ProxyProviderPackageCreators.createIntentWithSleepIntent(0))
                .setNextState(this.adapterSerializer.writeByte(adapterContext)));
    }

    private AdapterContext initAdapterContext(ByteBuffer callback, PaymentContext context) {
        AdapterContext adapterContext = AdapterStateUtils.getAdapterContext(context, this.adapterDeserializer);
        if (adapterContext.getStep() == Step.FINISH_THREE_DS) {
            Callback callbackObj = this.callbackDeserializer.read(callback.array());
            adapterContext.setPaRes(callbackObj.getPaRes());
            adapterContext.setMd(callbackObj.getMd());
        } else if (adapterContext.getStep() == Step.FINISH_THREE_DS_V2) {
            ThreeDsV2Callback callbackObj = this.threeDsV2CallbackDeserializer.read(callback.array());
            adapterContext.setPaRes(callbackObj.getCres());
        }
        log.info("AdapterContext: {} after callback.", adapterContext);
        return adapterContext;
    }

}