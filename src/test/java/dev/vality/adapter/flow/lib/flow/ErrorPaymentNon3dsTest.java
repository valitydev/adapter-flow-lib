package dev.vality.adapter.flow.lib.flow;

import dev.vality.adapter.flow.lib.client.RemoteClient;
import dev.vality.adapter.flow.lib.flow.config.AppConfig;
import dev.vality.adapter.flow.lib.flow.config.HandlerConfig;
import dev.vality.adapter.flow.lib.flow.config.ProcessorConfig;
import dev.vality.adapter.flow.lib.flow.utils.MockUtil;
import dev.vality.adapter.flow.lib.model.BaseResponseModel;
import dev.vality.adapter.flow.lib.utils.AdapterProperties;
import dev.vality.adapter.flow.lib.utils.CallbackUrlExtractor;
import dev.vality.adapter.flow.lib.utils.TimerProperties;
import dev.vality.bender.BenderSrv;
import dev.vality.cds.client.storage.CdsClientStorage;
import dev.vality.damsel.proxy_provider.PaymentContext;
import dev.vality.damsel.proxy_provider.PaymentProxyResult;
import dev.vality.damsel.proxy_provider.ProviderProxySrv;
import org.apache.thrift.TException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {HandlerConfig.class, AppConfig.class, ProcessorConfig.class,
        AdapterProperties.class, CallbackUrlExtractor.class, StepResolverImpl.class,
        TimerProperties.class})
@TestPropertySource(properties = {"error-mapping.file=classpath:fixture/errors.json"})
public class ErrorPaymentNon3dsTest {

    public static final String TEST_TRX_ID = "testTrxId";

    @Autowired
    private ProviderProxySrv.Iface serverHandlerLogDecorator;

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
                .errorCode("rem_error_21")
                .errorMessage("Remote service error!")
                .build();

        Mockito.when(client.auth(any())).thenReturn(baseResponseModel);
        Mockito.when(client.pay(any())).thenReturn(baseResponseModel);
    }


    @Test
    public void testErrorPayment() throws TException {
        // pay
        PaymentContext paymentContext = MockUtil.buildPaymentContext(String.valueOf(new Date().getTime()));
        paymentContext.getPaymentInfo().getPayment().setMakeRecurrent(true);

        PaymentProxyResult paymentProxyResult = serverHandlerLogDecorator.processPayment(paymentContext);
        assertTrue(paymentProxyResult.getIntent().isSetFinish());
        assertTrue(paymentProxyResult.getIntent().getFinish().getStatus().isSetFailure());
    }
}
