package dev.vality.adapter.flow.lib.flow;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.vality.adapter.flow.lib.client.RemoteClient;
import dev.vality.adapter.flow.lib.constant.Step;
import dev.vality.adapter.flow.lib.flow.config.AppConfig;
import dev.vality.adapter.flow.lib.flow.config.HandlerConfig;
import dev.vality.adapter.flow.lib.flow.config.ProcessorConfig;
import dev.vality.adapter.flow.lib.flow.utils.MockUtil;
import dev.vality.adapter.flow.lib.model.BaseResponseModel;
import dev.vality.adapter.flow.lib.utils.AdapterDeserializer;
import dev.vality.adapter.flow.lib.utils.AdapterProperties;
import dev.vality.adapter.flow.lib.utils.CallbackUrlExtractor;
import dev.vality.adapter.flow.lib.utils.TimerProperties;
import dev.vality.bender.BenderSrv;
import dev.vality.cds.client.storage.CdsClientStorage;
import dev.vality.damsel.domain.InvoicePaymentCaptured;
import dev.vality.damsel.domain.InvoicePaymentRefunded;
import dev.vality.damsel.domain.TargetInvoicePaymentStatus;
import dev.vality.damsel.proxy_provider.*;
import org.apache.thrift.TException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
@PropertySource("classpath:application.yaml")
@ContextConfiguration(classes = {HandlerConfig.class, AppConfig.class, ProcessorConfig.class,
        AdapterProperties.class, CallbackUrlExtractor.class, StepResolverImpl.class, TimerProperties.class})
@TestPropertySource(properties = {"server.rest.port=8083",
        "error-mapping.file=classpath:fixture/errors.json",
        "service.secret.enabled=true"})
public class ForwardRecurrentPaymentNon3dsTest {

    public static final String RECURRENT_TOKEN = "recurrentToken";
    public static final String TEST_TRX_ID = "testTrxId";

    @Autowired
    private ProviderProxySrv.Iface serverHandlerLogDecorator;

    protected AdapterDeserializer adapterDeserializer = new AdapterDeserializer(new ObjectMapper());

    @MockBean
    private CdsClientStorage cdsClientStorage;
    @MockBean
    protected BenderSrv.Iface benderClient;
    @MockBean
    protected RemoteClient client;

    @BeforeEach
    public void setUp() throws TException {
        MockitoAnnotations.openMocks(this);
        MockUtil.mockAllWithout3Ds(cdsClientStorage, benderClient);

        BaseResponseModel baseResponseModel = BaseResponseModel.builder()
                .providerTrxId(TEST_TRX_ID)
                .recurrentToken(RECURRENT_TOKEN)
                .build();

        Mockito.when(client.auth(any())).thenReturn(baseResponseModel);
        Mockito.when(client.pay(any())).thenReturn(baseResponseModel);
    }

    @Test
    public void testRecurrentForward() throws TException {
        // pay
        PaymentContext paymentContext = MockUtil.buildPaymentContext(String.valueOf(new Date().getTime()));
        paymentContext.getPaymentInfo().getPayment().setMakeRecurrent(true);

        PaymentProxyResult paymentProxyResult = serverHandlerLogDecorator.processPayment(paymentContext);
        assertNotNull(paymentProxyResult.getTrx().getId());
        assertTrue(paymentProxyResult.getIntent().isSetFinish());
        assertEquals(Step.DO_NOTHING, adapterDeserializer.read(paymentProxyResult.getNextState()).getNextStep());
        String token = paymentProxyResult.getIntent().getFinish().getStatus().getSuccess().getToken();
        assertNotNull(token);

        //capture
        paymentContext.getSession()
                .setTarget(TargetInvoicePaymentStatus.captured(new InvoicePaymentCaptured()))
                .setState(new byte[] {});
        paymentContext.getPaymentInfo().getPayment().setTrx(paymentProxyResult.getTrx());
        PaymentProxyResult paymentProxyResultDeposit = serverHandlerLogDecorator.processPayment(paymentContext);
        String tokenCapture = paymentProxyResult.getIntent().getFinish().getStatus().getSuccess().getToken();
        assertEquals(token, tokenCapture);
        assertEquals(Step.DO_NOTHING, adapterDeserializer.read(paymentProxyResultDeposit.getNextState()).getNextStep());

        //recurrent payment
        paymentContext = MockUtil.buildRecurrentPaymentContext(String.valueOf(new Date().getTime()), token);

        paymentProxyResult = serverHandlerLogDecorator.processPayment(paymentContext);
        assertNotNull(paymentProxyResult.getTrx().getId());
        assertEquals(Step.DO_NOTHING, adapterDeserializer.read(paymentProxyResult.getNextState()).getNextStep());

        // captured
        paymentContext
                .getSession()
                .setTarget(TargetInvoicePaymentStatus.captured(new InvoicePaymentCaptured()))
                .setState(paymentProxyResult.getNextState());
        paymentContext.getPaymentInfo().getPayment().setTrx(paymentProxyResult.getTrx());

        PaymentProxyResult paymentProxyResultOrderBinding = serverHandlerLogDecorator.processPayment(paymentContext);
        assertEquals(paymentProxyResult.getIntent().getFinish().getStatus().getSuccess(), new Success());
        assertEquals(Step.DO_NOTHING,
                adapterDeserializer.read(paymentProxyResultOrderBinding.getNextState()).getNextStep());
    }
}
