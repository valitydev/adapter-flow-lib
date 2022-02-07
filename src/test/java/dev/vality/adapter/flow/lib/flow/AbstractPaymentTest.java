package dev.vality.adapter.flow.lib.flow;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.vality.adapter.flow.lib.constant.Step;
import dev.vality.adapter.flow.lib.utils.AdapterDeserializer;
import dev.vality.damsel.domain.InvoicePaymentCaptured;
import dev.vality.damsel.domain.InvoicePaymentRefunded;
import dev.vality.damsel.domain.TargetInvoicePaymentStatus;
import dev.vality.damsel.proxy_provider.*;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

public class AbstractPaymentTest {

    public static final String TEST_TRX_ID = "testTrxId";

    @Autowired
    protected ProviderProxySrv.Iface serverHandlerLogDecorator;
    protected AdapterDeserializer adapterDeserializer = new AdapterDeserializer(new ObjectMapper());

    protected PaymentProxyResult checkSuccessAuthOrPay(PaymentContext paymentContext) throws TException {
        PaymentProxyResult paymentProxyResult = serverHandlerLogDecorator.processPayment(paymentContext);
        assertNotNull(paymentProxyResult.getTrx().getId());
        assertTrue(paymentProxyResult.getIntent().isSetFinish());
        assertEquals(Step.DO_NOTHING, adapterDeserializer.read(paymentProxyResult.getNextState()).getNextStep());
        return paymentProxyResult;
    }

    protected PaymentProxyResult checkSuccessCapture(PaymentContext paymentContext,
                                                     PaymentProxyResult paymentProxyResult,
                                                     byte[] state)
            throws TException {
        paymentContext.getSession()
                .setTarget(TargetInvoicePaymentStatus.captured(new InvoicePaymentCaptured()))
                .setState(state);
        paymentContext.getPaymentInfo().getPayment().setTrx(paymentProxyResult.getTrx());
        PaymentProxyResult paymentProxyResultDeposit = serverHandlerLogDecorator.processPayment(paymentContext);
        assertTrue(paymentProxyResultDeposit.getIntent().getFinish().getStatus().isSetSuccess());
        assertEquals(Step.DO_NOTHING, adapterDeserializer.read(paymentProxyResultDeposit.getNextState()).getNextStep());
        return paymentProxyResultDeposit;
    }

    protected void checkSuccessRefund(Long refundAmount, PaymentContext paymentContext,
                                      PaymentProxyResult paymentProxyResultDeposit)
            throws TException {
        paymentContext.getSession()
                .setTarget(TargetInvoicePaymentStatus.refunded(new InvoicePaymentRefunded()))
                .setState(paymentProxyResultDeposit.getNextState());
        paymentContext.getPaymentInfo()
                .setRefund(new InvoicePaymentRefund()
                        .setCash(new Cash(refundAmount, null)));
        PaymentProxyResult paymentProxyResultRefunded = serverHandlerLogDecorator.processPayment(paymentContext);
        assertEquals(paymentProxyResultRefunded.getIntent().getFinish().getStatus().getSuccess(), new Success());
    }

}
