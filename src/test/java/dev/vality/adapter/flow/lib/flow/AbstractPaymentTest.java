package dev.vality.adapter.flow.lib.flow;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.vality.adapter.flow.lib.client.RemoteClient;
import dev.vality.adapter.flow.lib.constant.Step;
import dev.vality.adapter.flow.lib.controller.ThreeDsCallbackController;
import dev.vality.adapter.flow.lib.flow.full.FullThreeDsAllVersionsStepResolverImpl;
import dev.vality.adapter.flow.lib.flow.full.GenerateTokenFullThreeDsAllVersionsStepResolverImpl;
import dev.vality.adapter.flow.lib.flow.config.AppConfig;
import dev.vality.adapter.flow.lib.flow.config.HandlerConfig;
import dev.vality.adapter.flow.lib.flow.config.ProcessorConfig;
import dev.vality.adapter.flow.lib.flow.config.TomcatEmbeddedConfiguration;
import dev.vality.adapter.flow.lib.service.TagManagementService;
import dev.vality.adapter.flow.lib.utils.CallbackUrlExtractor;
import dev.vality.adapter.flow.lib.utils.TemporaryContextDeserializer;
import dev.vality.adapter.flow.lib.utils.TimerProperties;
import dev.vality.adapter.helpers.hellgate.HellgateAdapterClient;
import dev.vality.bender.BenderSrv;
import dev.vality.cds.client.storage.CdsClientStorage;
import dev.vality.damsel.domain.InvoicePaymentCaptured;
import dev.vality.damsel.domain.InvoicePaymentRefunded;
import dev.vality.damsel.domain.TargetInvoicePaymentStatus;
import dev.vality.damsel.proxy_provider.*;
import dev.vality.java.damsel.utils.verification.ProxyProviderVerification;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;

import static dev.vality.java.damsel.utils.creators.DomainPackageCreators.createTargetProcessed;
import static org.junit.jupiter.api.Assertions.*;


@PropertySource("classpath:application.yaml")
@ContextConfiguration(classes = {HandlerConfig.class, AppConfig.class, ProcessorConfig.class,
        ThreeDsCallbackController.class, TomcatEmbeddedConfiguration.class, TagManagementService.class,
        CallbackUrlExtractor.class, FullThreeDsAllVersionsStepResolverImpl.class,
        GenerateTokenFullThreeDsAllVersionsStepResolverImpl.class, TimerProperties.class})
public class AbstractPaymentTest {

    @MockBean
    protected CdsClientStorage cdsClientStorage;
    @MockBean
    protected BenderSrv.Iface benderClient;
    @MockBean
    protected RemoteClient client;
    @MockBean
    protected HellgateAdapterClient hellgateAdapterClient;

    @Autowired
    protected ProviderProxySrv.Iface serverHandlerLogDecorator;

    protected TemporaryContextDeserializer temporaryContextDeserializer =
            new TemporaryContextDeserializer(new ObjectMapper());

    protected PaymentProxyResult checkSuccessAuthOrPay(PaymentContext paymentContext) throws TException {
        PaymentProxyResult paymentProxyResult = serverHandlerLogDecorator.processPayment(paymentContext);
        assertNotNull(paymentProxyResult.getTrx().getId());
        assertTrue(paymentProxyResult.getIntent().isSetFinish());
        assertEquals(Step.DO_NOTHING,
                temporaryContextDeserializer.read(paymentProxyResult.getNextState()).getNextStep());
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
        assertEquals(Step.DO_NOTHING,
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
