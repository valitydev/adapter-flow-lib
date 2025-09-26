package dev.vality.adapter.flow.lib.handler;

import dev.vality.adapter.flow.lib.logback.mdc.MdcContext;
import dev.vality.damsel.proxy_provider.PaymentCallbackResult;
import dev.vality.damsel.proxy_provider.PaymentContext;
import dev.vality.damsel.proxy_provider.PaymentProxyResult;
import dev.vality.damsel.proxy_provider.ProviderProxySrv;
import lombok.RequiredArgsConstructor;
import org.apache.thrift.TException;
import org.slf4j.MDC;

import java.nio.ByteBuffer;

@RequiredArgsConstructor
public class ServerHandlerMdcDecorator implements ProviderProxySrv.Iface {

    private final ProviderProxySrv.Iface serverHandlerLogDecorator;

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
