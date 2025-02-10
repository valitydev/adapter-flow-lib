package dev.vality.adapter.flow.lib.flow.simple.redirect;

import dev.vality.adapter.flow.lib.constant.OptionFields;
import dev.vality.adapter.flow.lib.constant.Stage;
import dev.vality.adapter.flow.lib.constant.Status;
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
import java.util.Map;

import static dev.vality.adapter.flow.lib.flow.simple.redirect.ForwardRecurrentPaymentNon3dsTest.RESULT_ID;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SimpleRedirectWithPollingDsFlowConfig.class)
@TestPropertySource(properties = {"server.rest.port=8083",
        "error-mapping.file=classpath:fixture/errors.json",
        "service.secret.enabled=true"})
public class PaymentNon3dsTest extends AbstractPaymentTest {

    @BeforeEach
    public void setUp() throws TException {
        MockitoAnnotations.openMocks(this);
        MockUtil.mockAllWithout3Ds(cdsStorageClient, benderClient);

        BaseResponseModel baseResponseModel = BeanUtils.createBaseResponseModel();
        Mockito.when(client.auth(any())).thenReturn(baseResponseModel);
        Mockito.when(client.pay(any())).thenReturn(baseResponseModel);
        Mockito.when(client.capture(any())).thenReturn(baseResponseModel);
        Mockito.when(client.status(any())).thenReturn(baseResponseModel);
    }

    @Test
    public void testPaymentOneStage() throws TException {
        // pay
        Map<String, String> options = MockUtil.buildOptionsOneStage();
        testPayment(options);
    }

    @Test
    public void testPaymentTwoStage() throws TException {
        // auth
        Map<String, String> options = MockUtil.buildOptionsTwoStage();
        testPayment(options);
    }

    private void testPayment(Map<String, String> options) throws TException {
        PaymentContext paymentContext = MockUtil.buildPaymentContext(String.valueOf(new Date().getTime()),
                options);
        PaymentProxyResult paymentProxyResult = processWithCheckStatusResult(paymentContext);
        paymentProxyResult = processWithCheckStatusResult(paymentContext, paymentProxyResult);

        BaseResponseModel successResult = BeanUtils.createBaseResponseModel();
        successResult.setProviderTrxId(RESULT_ID);
        successResult.setStatus(Status.SUCCESS);
        Mockito.when(client.status(any())).thenReturn(successResult);

        //capture
        if (Stage.ONE.equals(options.get(OptionFields.STAGE.name()))) {
            paymentProxyResult = checkSuccessCapture(paymentContext, paymentProxyResult, new byte[] {});
            processWithDoNothingSuccessResult(paymentContext, paymentProxyResult);
        } else {
            paymentProxyResult = processCaptureWithCheckStatusResult(paymentContext, paymentProxyResult, new byte[] {});
            processWithCheckStatusResult(paymentContext, paymentProxyResult);
        }
    }
}
