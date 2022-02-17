package dev.vality.adapter.flow.lib.handler;

import dev.vality.adapter.flow.lib.logback.mdc.MdcContext;
import dev.vality.damsel.proxy_provider.*;
import lombok.RequiredArgsConstructor;
import org.apache.thrift.TException;
import org.slf4j.MDC;

import java.nio.ByteBuffer;

@RequiredArgsConstructor
public class ServerHandlerMdcDecorator implements ProviderProxySrv.Iface {

    private final ProviderProxySrv.Iface serverHandlerLogDecorator;

    public RecurrentTokenProxyResult generateToken(RecurrentTokenContext recurrentTokenContext) throws TException {
        MdcContext.mdcPutContext(recurrentTokenContext);
        try {
            return serverHandlerLogDecorator.generateToken(recurrentTokenContext);
        } finally {
            MDC.clear();
        }
    }

    @Override
    public RecurrentTokenCallbackResult handleRecurrentTokenCallback(ByteBuffer byteBuffer,
                                                                     RecurrentTokenContext recurrentTokenContext)
            throws TException {
        MdcContext.mdcPutContext(recurrentTokenContext);
        try {
            return serverHandlerLogDecorator.handleRecurrentTokenCallback(byteBuffer, recurrentTokenContext);
        } finally {
            MDC.clear();
        }
    }

    @Override
    public PaymentProxyResult processPayment(PaymentContext paymentContext) throws TException {
        MdcContext.mdcPutContext(paymentContext);
        try {
            return serverHandlerLogDecorator.processPayment(paymentContext);
        } finally {
            MDC.clear();
        }
    }

    @Override
    public PaymentCallbackResult handlePaymentCallback(ByteBuffer byteBuffer, PaymentContext paymentContext)
            throws TException {
        MdcContext.mdcPutContext(paymentContext);
        try {
            return serverHandlerLogDecorator.handlePaymentCallback(byteBuffer, paymentContext);
        } finally {
            MDC.clear();
        }
    }

}
