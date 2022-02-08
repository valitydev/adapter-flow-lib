package dev.vality.adapter.flow.lib.handler.callback;

import dev.vality.adapter.common.handler.callback.CallbackHandler;
import dev.vality.adapter.common.model.AdapterContext;
import dev.vality.adapter.flow.lib.utils.AdapterDeserializer;
import dev.vality.adapter.flow.lib.utils.AdapterSerializer;
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

@Slf4j
@RequiredArgsConstructor
public class RecurrentTokenCallbackHandler
        implements CallbackHandler<RecurrentTokenCallbackResult, RecurrentTokenContext> {

    private final AdapterDeserializer adapterDeserializer;
    private final AdapterSerializer adapterSerializer;
//    private final CallbackDeserializer callbackDeserializer;
    private final ParametersDeserializer threeDsV2CallbackDeserializer;

    public RecurrentTokenCallbackResult handleCallback(ByteBuffer callback, RecurrentTokenContext context) {
        AdapterContext adapterContext = initAdapterContext(callback, context);
        byte[] callbackResponse = new byte[0];
        return ProxyProviderPackageCreators.createRecurrentTokenCallbackResult(callbackResponse,
                (new RecurrentTokenProxyResult()).setIntent(RecurrentTokenIntent
                                .sleep(ProxyProviderPackageCreators.createSleepIntent(
                                        BasePackageCreators.createTimerTimeout(0))))
                        .setNextState(this.adapterSerializer.writeByte(adapterContext))
        );
    }

    private AdapterContext initAdapterContext(ByteBuffer callback, RecurrentTokenContext context) {
//        AdapterContext adapterContext = AdapterStateUtils.getAdapterContext(context, this.adapterDeserializer);
//        if (adapterContext.getStep() == Step.GENERATE_TOKEN_FINISH_THREE_DS) {
//            Callback callbackObj = this.callbackDeserializer.read(callback.array());
//            adapterContext.setPaRes(callbackObj.getPaRes());
//            adapterContext.setMd(callbackObj.getMd());
//        } else if (adapterContext.getStep() == Step.GENERATE_TOKEN_FINISH_THREE_DS_V2) {
//            ThreeDsV2Callback callbackObj = this.threeDsV2CallbackDeserializer.read(callback.array());
//            adapterContext.setThreeDsMethodData(callbackObj.getThreeDSMethodData());
//            adapterContext.setPaRes(callbackObj.getCres());
//        }
//        log.info("AdapterContext: {} after callback.", adapterContext);
        return null;
    }

}