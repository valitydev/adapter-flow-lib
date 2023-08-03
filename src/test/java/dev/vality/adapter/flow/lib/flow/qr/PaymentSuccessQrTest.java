package dev.vality.adapter.flow.lib.flow.qr;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.vality.adapter.flow.lib.constant.OptionFields;
import dev.vality.adapter.flow.lib.constant.Stage;
import dev.vality.adapter.flow.lib.constant.Status;
import dev.vality.adapter.flow.lib.constant.Step;
import dev.vality.adapter.flow.lib.flow.AbstractPaymentTest;
import dev.vality.adapter.flow.lib.flow.qr.config.QrRedirectWithPollingDsFlowConfig;
import dev.vality.adapter.flow.lib.flow.utils.BeanUtils;
import dev.vality.adapter.flow.lib.flow.utils.MockUtil;
import dev.vality.adapter.flow.lib.model.BaseResponseModel;
import dev.vality.damsel.proxy_provider.PaymentContext;
import dev.vality.damsel.proxy_provider.PaymentProxyResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

@Slf4j
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = QrRedirectWithPollingDsFlowConfig.class)
@TestPropertySource(properties = {"error-mapping.file=classpath:fixture/errors.json",
        "adapter.callbackUrl=http://localhost:8080/test",
        "server.rest.endpoint=adapter",
        "server.rest.port=8083"})
public class PaymentSuccessQrTest extends AbstractPaymentTest {

    @BeforeEach
    public void setUp() throws TException {
        MockitoAnnotations.openMocks(this);

        MockUtil.mockIdGenerator(benderClient);

        BaseResponseModel baseResponseModel = BeanUtils.createBaseResponseModel();
        baseResponseModel.setQrDisplayData(BeanUtils.createQrDisplayData());
        baseResponseModel.setStatus(Status.NEED_REDIRECT);

        Mockito.when(client.auth(any())).thenReturn(baseResponseModel);
        Mockito.when(client.pay(any())).thenReturn(baseResponseModel);

        BaseResponseModel successResponseModel = BeanUtils.createBaseResponseModel();
        Mockito.when(client.capture(any())).thenReturn(successResponseModel);
        Mockito.when(client.refund(any())).thenReturn(successResponseModel);
        Mockito.when(client.status(any())).thenReturn(successResponseModel);
    }

    @Test
    public void testOneStage() throws TException, IOException {
        // auth
        Map<String, String> options = MockUtil.buildOptionsOneStage();
        testQr(options);
    }

    @Test
    public void testTwoStage() throws TException, IOException {
        // auth
        Map<String, String> options = MockUtil.buildOptionsTwoStage();
        testQr(options);
    }

    private void testQr(Map<String, String> options) throws TException, JsonProcessingException {
        PaymentContext paymentContext = MockUtil.buildPaymentContextPaymentTerminal("invoice_id", options);

        PaymentProxyResult paymentProxyResult = serverHandlerLogDecorator.processPayment(paymentContext);
        assertTrue(paymentProxyResult.getIntent().getSuspend().getUserInteraction().isSetQrCodeDisplayRequest());
        assertEquals(Step.CHECK_STATUS,
                temporaryContextDeserializer.read(paymentProxyResult.getNextState()).getNextStep());

        //checkStatus
        paymentProxyResult = processWithDoNothingSuccessResult(paymentContext, paymentProxyResult);

        //capture
        if (Stage.ONE.equals(options.get(OptionFields.STAGE.name()))) {
            paymentProxyResult = checkSuccessCapture(paymentContext, paymentProxyResult, new byte[] {});
            processWithDoNothingSuccessResult(paymentContext, paymentProxyResult);
        } else {
            paymentProxyResult = processCaptureWithCheckStatusResult(paymentContext, paymentProxyResult, new byte[] {});
            processWithCheckStatusResult(paymentContext, paymentProxyResult);
        }

        //refund
        checkSuccessRefund(1100L, paymentContext, paymentProxyResult);
    }

}
