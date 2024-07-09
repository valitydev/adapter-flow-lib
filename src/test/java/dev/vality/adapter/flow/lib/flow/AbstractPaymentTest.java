package dev.vality.adapter.flow.lib.flow;

import dev.vality.adapter.common.cds.CdsStorageClient;
import dev.vality.adapter.common.damsel.ProxyProviderVerification;
import dev.vality.adapter.common.hellgate.HellgateClient;
import dev.vality.adapter.flow.lib.client.RemoteClient;
import dev.vality.adapter.flow.lib.constant.Step;
import dev.vality.adapter.flow.lib.controller.ThreeDsCallbackController;
import dev.vality.adapter.flow.lib.flow.config.*;
import dev.vality.adapter.flow.lib.flow.full.FullThreeDsAllVersionsStepResolverImpl;
import dev.vality.adapter.flow.lib.flow.full.GenerateTokenFullThreeDsAllVersionsStepResolverImpl;
import dev.vality.adapter.flow.lib.serde.TemporaryContextDeserializer;
import dev.vality.adapter.flow.lib.service.TagManagementServiceImpl;
import dev.vality.adapter.flow.lib.service.impl.CallbackUrlExtractorImpl;
import dev.vality.adapter.flow.lib.validator.AdapterConfigurationValidator;
import dev.vality.bender.BenderSrv;
import dev.vality.damsel.domain.InvoicePaymentCaptured;
import dev.vality.damsel.domain.InvoicePaymentRefunded;
import dev.vality.damsel.domain.TargetInvoicePaymentStatus;
import dev.vality.damsel.proxy_provider.*;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;

import static dev.vality.adapter.common.damsel.DomainPackageCreators.createTargetProcessed;
import static org.junit.jupiter.api.Assertions.*;

@PropertySource("classpath:application.yaml")
@ContextConfiguration(classes = {HandlerConfig.class, AppConfig.class, ProcessorConfig.class, SerdeConfig.class,
        ThreeDsCallbackController.class, TomcatEmbeddedConfiguration.class, TagManagementServiceImpl.class,
        CallbackUrlExtractorImpl.class, FullThreeDsAllVersionsStepResolverImpl.class,
        GenerateTokenFullThreeDsAllVersionsStepResolverImpl.class})
public class AbstractPaymentTest {

    @MockBean
    protected CdsStorageClient cdsStorageClient;
    @MockBean
    protected BenderSrv.Iface benderClient;
    @MockBean
    protected RemoteClient client;
    @MockBean
    protected HellgateClient hellgateClient;
    @MockBean
    protected AdapterConfigurationValidator paymentContextValidator;

    @Autowired
    protected ProviderProxySrv.Iface serverHandlerLogDecorator;
    @Autowired
    public TemporaryContextDeserializer temporaryContextDeserializer;

    protected PaymentProxyResult processWithDoNothingSuccessResult(PaymentContext paymentContext) throws TException {
        PaymentProxyResult paymentProxyResult = serverHandlerLogDecorator.processPayment(paymentContext);
        assertNotNull(paymentProxyResult.getTrx().getId());
        assertTrue(paymentProxyResult.getIntent().isSetFinish());
        assertEquals(Step.DO_NOTHING,
                temporaryContextDeserializer.read(paymentProxyResult.getNextState()).getNextStep());
        return paymentProxyResult;
    }

    protected PaymentProxyResult processWithDoNothingSuccessResult(PaymentContext paymentContext,
                                                                   PaymentProxyResult paymentProxyResult)
            throws TException {
        paymentContext.getSession()
                .setState(paymentProxyResult.getNextState());
        PaymentProxyResult paymentProxyResultDeposit = serverHandlerLogDecorator.processPayment(paymentContext);
        assertTrue(paymentProxyResultDeposit.getIntent().isSetFinish());
        assertTrue(paymentProxyResultDeposit.getIntent().getFinish().getStatus().isSetSuccess());
        assertEquals(Step.DO_NOTHING,
                temporaryContextDeserializer.read(paymentProxyResultDeposit.getNextState()).getNextStep());
        return paymentProxyResultDeposit;
    }

    protected PaymentProxyResult processWithCheckStatusResult(PaymentContext paymentContext) throws TException {
        PaymentProxyResult paymentProxyResult = serverHandlerLogDecorator.processPayment(paymentContext);
        assertTrue(paymentProxyResult.getIntent().isSetSleep());
        assertEquals(Step.CHECK_STATUS,
                temporaryContextDeserializer.read(paymentProxyResult.getNextState()).getNextStep());
        return paymentProxyResult;
    }


    protected PaymentProxyResult processWithCheckStatusResult(PaymentContext paymentContext,
                                                              PaymentProxyResult paymentProxyResult)
            throws TException {
        paymentContext.getSession()
                .setState(paymentProxyResult.getNextState());
        PaymentProxyResult paymentProxyResultDeposit = serverHandlerLogDecorator.processPayment(paymentContext);
        assertTrue(paymentProxyResult.getIntent().isSetSleep());
        assertEquals(Step.CHECK_STATUS,
                temporaryContextDeserializer.read(paymentProxyResult.getNextState()).getNextStep());
        return paymentProxyResultDeposit;
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
        assertEquals(Step.DO_NOTHING,
                temporaryContextDeserializer.read(paymentProxyResultDeposit.getNextState()).getNextStep());
        return paymentProxyResultDeposit;
    }

    protected PaymentProxyResult processCaptureWithCheckStatusResult(PaymentContext paymentContext,
                                                                     PaymentProxyResult paymentProxyResult,
                                                                     byte[] state)
            throws TException {
        paymentContext.getSession()
                .setTarget(TargetInvoicePaymentStatus.captured(new InvoicePaymentCaptured()))
                .setState(state);
        paymentContext.getPaymentInfo().getPayment().setTrx(paymentProxyResult.getTrx());
        PaymentProxyResult paymentProxyResultDeposit = serverHandlerLogDecorator.processPayment(paymentContext);
        assertTrue(paymentProxyResultDeposit.getIntent().isSetSleep());
        assertEquals(Step.CHECK_STATUS,
                temporaryContextDeserializer.read(paymentProxyResultDeposit.getNextState()).getNextStep());
        return paymentProxyResultDeposit;
    }

    protected void checkSuccessRefund(Long refundAmount,
                                      PaymentContext paymentContext,
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

    protected PaymentProxyResult checkSuccessFinishThreeDs(PaymentContext context,
                                                           PaymentProxyResult proxyResult,
                                                           PaymentCallbackResult paymentCallbackResult)
            throws TException {
        context.getPaymentInfo().getPayment().setTrx(proxyResult.getTrx());
        context.getSession().setState(paymentCallbackResult.getResult().getNextState());
        context.getSession().setTarget(createTargetProcessed());
        PaymentProxyResult paymentProxyResult = serverHandlerLogDecorator.processPayment(context);
        String trxId = paymentProxyResult.getTrx().getId();
        assertTrue(ProxyProviderVerification.isSuccess(paymentProxyResult));
        assertEquals(trxId, paymentProxyResult.getTrx().getId());
        return paymentProxyResult;
    }

    protected PaymentProxyResult checkSuspend(Step step,
                                              PaymentContext context,
                                              PaymentProxyResult proxyResult,
                                              PaymentCallbackResult paymentCallbackResult)
            throws TException {
        context.getPaymentInfo().getPayment().setTrx(proxyResult.getTrx());
        context.getSession().setState(paymentCallbackResult.getResult().getNextState());
        context.getSession().setTarget(createTargetProcessed());
        PaymentProxyResult paymentProxyResult = serverHandlerLogDecorator.processPayment(context);
        assertTrue(paymentProxyResult.getIntent().getSuspend().getUserInteraction().isSetRedirect());
        assertEquals(step, temporaryContextDeserializer.read(paymentProxyResult.getNextState()).getNextStep());
        return paymentProxyResult;
    }
}
