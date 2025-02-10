package dev.vality.adapter.flow.lib.utils;

import dev.vality.adapter.flow.lib.constant.TargetStatus;
import dev.vality.adapter.flow.lib.exception.UnknownTargetStatusException;
import dev.vality.damsel.domain.TargetInvoicePaymentStatus;
import dev.vality.damsel.proxy_provider.PaymentContext;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TargetStatusResolver {

    public static TargetStatus extractTargetStatus(PaymentContext paymentContext) {
        if (paymentContext == null) {
            throw new IllegalArgumentException("PaymentContext cannot be empty");
        } else if (paymentContext.getSession() == null) {
            throw new IllegalArgumentException("Payment context session cannot be empty");
        } else {
            return extractTargetStatus(paymentContext.getSession().getTarget());
        }
    }

    public static TargetStatus extractTargetStatus(TargetInvoicePaymentStatus targetInvoicePaymentStatus) {
        if (targetInvoicePaymentStatus != null) {
            if (targetInvoicePaymentStatus.isSetProcessed()) {
                return TargetStatus.PROCESSED;
            } else if (targetInvoicePaymentStatus.isSetCancelled()) {
                return TargetStatus.CANCELLED;
            } else if (targetInvoicePaymentStatus.isSetCaptured()) {
                return TargetStatus.CAPTURED;
            } else if (targetInvoicePaymentStatus.isSetRefunded()) {
                return TargetStatus.REFUNDED;
            }
        }
        throw new UnknownTargetStatusException();
    }
}
