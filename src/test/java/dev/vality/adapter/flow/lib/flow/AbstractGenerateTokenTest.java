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
import dev.vality.damsel.proxy_provider.*;
import dev.vality.java.damsel.utils.verification.ProxyProviderVerification;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;

import static dev.vality.adapter.flow.lib.flow.full.three.ds.ForwardRecurrentPaymentNon3dsTest.RECURRENT_TOKEN;
import static dev.vality.java.damsel.utils.creators.DomainPackageCreators.createTargetProcessed;
import static org.junit.jupiter.api.Assertions.*;


@PropertySource("classpath:application.yaml")
@ContextConfiguration(classes = {HandlerConfig.class, AppConfig.class, ProcessorConfig.class,
        ThreeDsCallbackController.class, TomcatEmbeddedConfiguration.class, TagManagementService.class,
        CallbackUrlExtractor.class, FullThreeDsAllVersionsStepResolverImpl.class,
        GenerateTokenFullThreeDsAllVersionsStepResolverImpl.class, TimerProperties.class})
public class AbstractGenerateTokenTest {

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
}
