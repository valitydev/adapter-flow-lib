package dev.vality.adapter.flow.lib.handler;

import dev.vality.adapter.flow.lib.utils.PaymentResourceTypeResolver;
import dev.vality.damsel.proxy_provider.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;

import java.nio.ByteBuffer;

import static dev.vality.adapter.common.damsel.ProxyProviderPackageExtractors.*;
import static dev.vality.adapter.common.damsel.ProxyProviderVerification.isUndefinedResultOrUnavailable;

@Slf4j
@RequiredArgsConstructor
public class ServerHandlerLogDecorator implements ProviderProxySrv.Iface {

    private final ProviderProxySrv.Iface handler;

    @Override
    public RecurrentTokenProxyResult generateToken(RecurrentTokenContext context) throws TException {
        String recurrentId = extractRecurrentId(context);
        log.info("Generate token started with recurrentId {}", recurrentId);
        try {
            RecurrentTokenProxyResult proxyResult = handler.generateToken(context);
            log.info("Generate token finished {} with recurrentId {}", proxyResult, recurrentId);
            return proxyResult;
        } catch (Exception ex) {
            String message = "Failed handle generate token with recurrentId " + recurrentId;
            logMessage(ex, message);
            throw ex;
        }
    }

    @Override
    public RecurrentTokenCallbackResult handleRecurrentTokenCallback(ByteBuffer byteBuffer,
                                                                     RecurrentTokenContext context) throws TException {
        String recurrentId = extractRecurrentId(context);
        log.info("handleRecurrentTokenCallback: start with recurrentId {}", recurrentId);
        RecurrentTokenCallbackResult result = handler.handleRecurrentTokenCallback(byteBuffer, context);
        log.info("handleRecurrentTokenCallback end {} with recurrentId {}", result, recurrentId);
        return result;
    }

    @Override
    public PaymentProxyResult processPayment(PaymentContext context) throws TException {
        String invoiceId = extractInvoiceId(context);
        String invoicePaymentStatus = extractTargetInvoicePaymentStatus(context);
        String paymentResourceType = PaymentResourceTypeResolver.extractPaymentResourceType(context);
        log.info("Process payment handle resource='{}', status='{}' start with invoiceId {}", paymentResourceType,
                invoicePaymentStatus, invoiceId);
        try {
            PaymentProxyResult proxyResult = handler.processPayment(context);
            log.info("Process payment handle resource='{}', status='{}' finished with invoiceId {} and proxyResult {}",
                    paymentResourceType, invoicePaymentStatus, invoiceId, proxyResult);
            return proxyResult;
        } catch (Exception e) {
            String message = String.format(
                    "Failed handle resource=%s, status=%s process payment for operation with invoiceId %s",
                    paymentResourceType, invoicePaymentStatus, invoiceId);
            logMessage(e, message);
            throw e;
        }
    }

    @Override
    public PaymentCallbackResult handlePaymentCallback(ByteBuffer byteBuffer,
                                                       PaymentContext context) throws TException {
        String invoiceId = extractInvoiceId(context);
        log.info("handlePaymentCallback start with invoiceId {}", invoiceId);
        PaymentCallbackResult result = handler.handlePaymentCallback(byteBuffer, context);
        log.info("handlePaymentCallback finish {} with invoiceId {}", result, invoiceId);
        return result;
    }

    private static void logMessage(Exception ex, String message) {
        if (isUndefinedResultOrUnavailable(ex)) {
            log.warn(message, ex);
        } else {
            log.error(message, ex);
        }
    }
}
