package dev.vality.adapter.flow.lib.handler;

import dev.vality.adapter.common.Validator;
import dev.vality.adapter.common.handler.callback.CallbackHandler;
import dev.vality.adapter.flow.lib.converter.entry.CtxToEntryModelConverter;
import dev.vality.adapter.flow.lib.converter.entry.RecCtxToEntryModelConverter;
import dev.vality.adapter.flow.lib.converter.exit.ExitModelToProxyResultConverter;
import dev.vality.adapter.flow.lib.converter.exit.ExitModelToRecTokenProxyResultConverter;
import dev.vality.damsel.proxy_provider.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdapterServerHandler implements ProviderProxySrv.Iface {

    private final Validator<PaymentContext> paymentContextValidator;
    private final Validator<RecurrentTokenContext> recurrentTokenContextValidator;

    private final CallbackHandler<PaymentCallbackResult, PaymentContext> paymentCallbackHandler;
    private final CallbackHandler<RecurrentTokenCallbackResult, RecurrentTokenContext> recurrentTokenCallbackHandler;
    private final CtxToEntryModelConverter ctxToEntryModelConverter;
    private final RecCtxToEntryModelConverter recCtxToEntryStateModelConverter;
    private final ExitModelToRecTokenProxyResultConverter exitModelToRecTokenProxyResultConverter;
    private final ExitModelToProxyResultConverter exitModelToProxyResultConverter;
    private final ServerFlowHandler serverFlowHandler;
    private final ServerFlowHandler generateTokenFlowHandler;

    @Override
    public RecurrentTokenProxyResult generateToken(RecurrentTokenContext context) throws TException {
        return generateTokenFlowHandler.handle(recurrentTokenContextValidator,
                recCtxToEntryStateModelConverter,
                exitModelToRecTokenProxyResultConverter,
                context);
    }

    @Override
    public RecurrentTokenCallbackResult handleRecurrentTokenCallback(
            ByteBuffer byteBuffer,
            RecurrentTokenContext context
    ) throws TException {
        recurrentTokenContextValidator.validate(context);
        return recurrentTokenCallbackHandler.handleCallback(byteBuffer, context);
    }

    @Override
    public PaymentProxyResult processPayment(PaymentContext context) throws TException {
        return serverFlowHandler.handle(paymentContextValidator,
                ctxToEntryModelConverter,
                exitModelToProxyResultConverter,
                context);
    }

    @Override
    public PaymentCallbackResult handlePaymentCallback(ByteBuffer byteBuffer, PaymentContext context)
            throws TException {
        paymentContextValidator.validate(context);
        return paymentCallbackHandler.handleCallback(byteBuffer, context);
    }

}
