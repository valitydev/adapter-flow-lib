package dev.vality.adapter.flow.lib.handler;

import dev.vality.adapter.flow.lib.handler.callback.CallbackHandler;
import dev.vality.adapter.flow.lib.validator.AdapterConfigurationValidator;
import dev.vality.damsel.proxy_provider.PaymentCallbackResult;
import dev.vality.damsel.proxy_provider.PaymentContext;
import dev.vality.damsel.proxy_provider.PaymentProxyResult;
import dev.vality.damsel.proxy_provider.ProviderProxySrv;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;

import java.nio.ByteBuffer;

@Slf4j
@RequiredArgsConstructor
public class ProxyProviderServiceImpl implements ProviderProxySrv.Iface {

    private final CallbackHandler<PaymentCallbackResult, PaymentContext> paymentCallbackHandler;
    private final ServerFlowHandler<PaymentContext, PaymentProxyResult> serverFlowHandler;
    private final AdapterConfigurationValidator adapterConfigurationValidator;

    @Override
    public PaymentProxyResult processPayment(PaymentContext context) throws TException {
        adapterConfigurationValidator.validate(context.getOptions());
        return serverFlowHandler.handle(context);
    }

    @Override
    public PaymentCallbackResult handlePaymentCallback(ByteBuffer byteBuffer, PaymentContext context)
            throws TException {
        return paymentCallbackHandler.handleCallback(byteBuffer, context);
    }

}
