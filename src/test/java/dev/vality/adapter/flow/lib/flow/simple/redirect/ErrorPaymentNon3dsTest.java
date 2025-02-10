package dev.vality.adapter.flow.lib.flow.simple.redirect;

import dev.vality.adapter.flow.lib.flow.AbstractPaymentTest;
import dev.vality.adapter.flow.lib.flow.simple.redirect.config.SimpleRedirectWithPollingDsFlowConfig;
import dev.vality.adapter.flow.lib.flow.utils.BeanUtils;
import dev.vality.adapter.flow.lib.flow.utils.MockUtil;
import dev.vality.adapter.flow.lib.model.BaseResponseModel;
import dev.vality.damsel.proxy_provider.PaymentContext;
import dev.vality.damsel.proxy_provider.PaymentProxyResult;
import org.apache.thrift.TException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SimpleRedirectWithPollingDsFlowConfig.class)
@TestPropertySource(properties = {"server.rest.port=8083",
        "error-mapping.file=classpath:fixture/errors.json"})
public class ErrorPaymentNon3dsTest extends AbstractPaymentTest {

    @BeforeEach
    public void setUp() throws TException {
        MockitoAnnotations.openMocks(this);
        MockUtil.mockAllWithout3Ds(cdsStorageClient, benderClient);

        BaseResponseModel baseResponseModel = BeanUtils.createBaseResponseModel();
        baseResponseModel.setErrorCode("rem_error_21");
        baseResponseModel.setErrorMessage("Remote service error!");

        Mockito.when(client.auth(any())).thenReturn(baseResponseModel);
        Mockito.when(client.pay(any())).thenReturn(baseResponseModel);
    }

    @Test
    public void testErrorPayment() throws TException {
        // pay
        PaymentContext paymentContext = MockUtil.buildPaymentContext(String.valueOf(new Date().getTime()),
                MockUtil.buildOptionsOneStage());

        PaymentProxyResult paymentProxyResult = serverHandlerLogDecorator.processPayment(paymentContext);
        assertTrue(paymentProxyResult.getIntent().isSetFinish());
        assertTrue(paymentProxyResult.getIntent().getFinish().getStatus().isSetFailure());
    }
}
