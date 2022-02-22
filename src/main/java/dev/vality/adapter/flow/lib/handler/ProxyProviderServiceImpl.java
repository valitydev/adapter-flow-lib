package dev.vality.adapter.flow.lib.handler;

import dev.vality.adapter.flow.lib.handler.callback.CallbackHandler;
import dev.vality.adapter.flow.lib.validator.AdapterConfigurationValidator;
import dev.vality.damsel.proxy_provider.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;

import java.nio.ByteBuffer;

@Slf4j
@RequiredArgsConstructor
public class ProxyProviderServiceImpl implements ProviderProxySrv.Iface {

    private final CallbackHandler<PaymentCallbackResult, PaymentContext> paymentCallbackHandler;
    private final CallbackHandler<RecurrentTokenCallbackResult, RecurrentTokenContext> recurrentTokenCallbackHandler;
    private final ServerFlowHandler<PaymentContext, PaymentProxyResult> serverFlowHandler;
    private final ServerFlowHandler<RecurrentTokenContext, RecurrentTokenProxyResult> generateTokenFlowHandler;
    private final AdapterConfigurationValidator adapterConfigurationValidator;

    @Override
    public RecurrentTokenProxyResult generateToken(RecurrentTokenContext context) throws TException {
        adapterConfigurationValidator.validate(context.getOptions());
        return generateTokenFlowHandler.handle(context);
    }

    @Override
    public RecurrentTokenCallbackResult handleRecurrentTokenCallback(
            ByteBuffer byteBuffer,
            RecurrentTokenContext context
    ) throws TException {
        return recurrentTokenCallbackHandler.handleCallback(byteBuffer, context);
    }

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
