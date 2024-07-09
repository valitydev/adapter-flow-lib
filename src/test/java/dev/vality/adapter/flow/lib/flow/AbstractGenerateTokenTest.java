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
import dev.vality.damsel.proxy_provider.*;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;

import static dev.vality.adapter.common.damsel.DomainPackageCreators.createTargetProcessed;
import static dev.vality.adapter.flow.lib.flow.full.three.ds.ForwardRecurrentPaymentNon3dsTest.RECURRENT_TOKEN;
import static org.junit.jupiter.api.Assertions.*;

@PropertySource("classpath:application.yaml")
@ContextConfiguration(classes = {HandlerConfig.class, AppConfig.class, ProcessorConfig.class, SerdeConfig.class,
        ThreeDsCallbackController.class, TomcatEmbeddedConfiguration.class, TagManagementServiceImpl.class,
        CallbackUrlExtractorImpl.class, FullThreeDsAllVersionsStepResolverImpl.class,
        GenerateTokenFullThreeDsAllVersionsStepResolverImpl.class})
public class AbstractGenerateTokenTest {

    @MockBean
    protected CdsStorageClient cdsStorageClient;
    @MockBean
    protected AdapterConfigurationValidator paymentContextValidator;
    @MockBean
    protected BenderSrv.Iface benderClient;
    @MockBean
    protected RemoteClient client;
    @MockBean
    protected HellgateClient hellgateClient;
    @Autowired
    public TemporaryContextDeserializer temporaryContextDeserializer;

    @Autowired
    protected ProviderProxySrv.Iface serverHandlerLogDecorator;

    protected RecurrentTokenProxyResult checkSuccessAuthOrPay(RecurrentTokenContext paymentContext) throws TException {
        RecurrentTokenProxyResult recurrentTokenProxyResult = serverHandlerLogDecorator.generateToken(paymentContext);
        assertNotNull(recurrentTokenProxyResult.getTrx().getId());
        assertTrue(recurrentTokenProxyResult.getIntent().isSetSleep());
        assertEquals(Step.CAPTURE,
                temporaryContextDeserializer.read(recurrentTokenProxyResult.getNextState()).getNextStep());
        return recurrentTokenProxyResult;
    }

    protected RecurrentTokenProxyResult checkSleepWithStatus(Step step,
                                                             RecurrentTokenContext recurrentTokenContext,
                                                             RecurrentTokenProxyResult recurrentTokenProxyResult,
                                                             byte[] state)
            throws TException {
        recurrentTokenContext.getSession()
                .setState(state);
        recurrentTokenContext.getTokenInfo().setTrx(recurrentTokenProxyResult.getTrx());
        RecurrentTokenProxyResult paymentProxyResultDeposit =
                serverHandlerLogDecorator.generateToken(recurrentTokenContext);
        assertTrue(paymentProxyResultDeposit.getIntent().isSetSleep());
        assertEquals(step, temporaryContextDeserializer.read(paymentProxyResultDeposit.getNextState()).getNextStep());
        return paymentProxyResultDeposit;
    }

    protected RecurrentTokenProxyResult checkSuccessRefund(RecurrentTokenContext recurrentTokenContext,
                                                           RecurrentTokenProxyResult recurrentTokenProxyResult,
                                                           byte[] state)
            throws TException {
        recurrentTokenContext.getSession().setState(state);
        recurrentTokenContext.getTokenInfo().setTrx(recurrentTokenProxyResult.getTrx());
        RecurrentTokenProxyResult paymentProxyResultRefunded =
                serverHandlerLogDecorator.generateToken(recurrentTokenContext);
        assertEquals(paymentProxyResultRefunded.getIntent().getFinish().getStatus().getSuccess(),
                new RecurrentTokenSuccess(RECURRENT_TOKEN));
        assertNotNull(paymentProxyResultRefunded.getIntent().getFinish().getStatus().getSuccess().getToken());
        assertEquals(Step.DO_NOTHING,
                temporaryContextDeserializer.read(paymentProxyResultRefunded.getNextState()).getNextStep());
        return paymentProxyResultRefunded;
    }

    protected RecurrentTokenProxyResult checkSuccessFinishThreeDs(RecurrentTokenContext context,
                                                                  RecurrentTokenProxyResult proxyResult,
                                                                  RecurrentTokenCallbackResult paymentCallbackResult)
            throws TException {
        context.getTokenInfo().setTrx(proxyResult.getTrx());
        context.getSession().setState(paymentCallbackResult.getResult().getNextState());
        RecurrentTokenProxyResult paymentProxyResult = serverHandlerLogDecorator.generateToken(context);
        String trxId = paymentProxyResult.getTrx().getId();
        assertTrue(ProxyProviderVerification.isSleep(paymentProxyResult));
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

    protected RecurrentTokenProxyResult processWithDoNothingSuccessResult(RecurrentTokenContext paymentContext,
                                                                          RecurrentTokenProxyResult paymentProxyResult)
            throws TException {
        paymentContext.getSession().setState(paymentProxyResult.getNextState());
        RecurrentTokenProxyResult recurrentTokenProxyResult = serverHandlerLogDecorator.generateToken(paymentContext);
        assertTrue(recurrentTokenProxyResult.getIntent().isSetFinish());
        assertTrue(recurrentTokenProxyResult.getIntent().getFinish().getStatus().isSetSuccess());
        assertEquals(Step.DO_NOTHING,
                temporaryContextDeserializer.read(recurrentTokenProxyResult.getNextState()).getNextStep());
        return recurrentTokenProxyResult;
    }
}
