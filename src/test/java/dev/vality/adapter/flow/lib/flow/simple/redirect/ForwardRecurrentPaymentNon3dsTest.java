package dev.vality.adapter.flow.lib.flow.simple.redirect;

import dev.vality.adapter.flow.lib.constant.OptionFields;
import dev.vality.adapter.flow.lib.constant.Stage;
import dev.vality.adapter.flow.lib.constant.Status;
import dev.vality.adapter.flow.lib.constant.Step;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SimpleRedirectWithPollingDsFlowConfig.class)
@TestPropertySource(properties = {"server.rest.port=8083",
        "error-mapping.file=classpath:fixture/errors.json",
        "service.secret.enabled=true"})
public class ForwardRecurrentPaymentNon3dsTest extends AbstractPaymentTest {

    public static final String RECURRENT_TOKEN = "recurrentToken";
    public static final String RESULT_ID = "RESULT_ID";

    @BeforeEach
    public void setUp() throws TException {
        MockitoAnnotations.openMocks(this);
        MockUtil.mockAllWithout3Ds(cdsStorageClient, benderClient);

        BaseResponseModel baseResponseModel = BeanUtils.createBaseResponseModel();
        baseResponseModel.setRecurrentToken(RECURRENT_TOKEN);
        baseResponseModel.setStatus(Status.NEED_RETRY);

        Mockito.when(client.auth(any())).thenReturn(baseResponseModel);
        Mockito.when(client.pay(any())).thenReturn(baseResponseModel);
        Mockito.when(client.status(any())).thenReturn(baseResponseModel);
        Mockito.when(client.capture(any())).thenReturn(baseResponseModel);
    }

    @Test
    public void testOneStage() throws TException {
        // pay
        Map<String, String> options = MockUtil.buildOptionsOneStage();
        testRecurrentForward(options);
    }

    @Test
    public void testTwoStage() throws TException {
        // pay
        Map<String, String> options = MockUtil.buildOptionsTwoStage();
        testRecurrentForward(options);
    }

    private void testRecurrentForward(Map<String, String> options) throws TException {
        PaymentContext paymentContext = MockUtil.buildPaymentContext(String.valueOf(new Date().getTime()), options);
        paymentContext.getPaymentInfo().getPayment().setMakeRecurrent(true);

        PaymentProxyResult paymentProxyResult = processWithCheckStatusResult(paymentContext);

        paymentProxyResult = processWithCheckStatusResult(paymentContext, paymentProxyResult);

        BaseResponseModel successResult = BeanUtils.createBaseResponseModel();
        successResult.setProviderTrxId(RESULT_ID);
        successResult.setRecurrentToken(RECURRENT_TOKEN);
        successResult.setStatus(Status.SUCCESS);
        Mockito.when(client.status(any())).thenReturn(successResult);

        paymentProxyResult = processWithDoNothingSuccessResult(paymentContext, paymentProxyResult);
        String token = paymentProxyResult.getIntent().getFinish().getStatus().getSuccess().getToken();

        BaseResponseModel baseResponseModel = BeanUtils.createBaseResponseModel();
        baseResponseModel.setStatus(Status.SUCCESS);
        Mockito.when(client.capture(any())).thenReturn(baseResponseModel);

        //capture
        if (Stage.ONE.equals(options.get(OptionFields.STAGE.name()))) {
            paymentProxyResult = checkSuccessCapture(paymentContext, paymentProxyResult, new byte[] {});
            paymentProxyResult = processWithDoNothingSuccessResult(paymentContext, paymentProxyResult);
            String tokenCapture = paymentProxyResult.getIntent().getFinish().getStatus().getSuccess().getToken();
            assertEquals(token, tokenCapture);
        } else {
            paymentProxyResult = processCaptureWithCheckStatusResult(paymentContext, paymentProxyResult, new byte[] {});
            paymentProxyResult = processWithCheckStatusResult(paymentContext, paymentProxyResult);
            String tokenCapture = paymentProxyResult.getIntent().getFinish().getStatus().getSuccess().getToken();
            assertEquals(token, tokenCapture);
        }


        baseResponseModel = BeanUtils.createBaseResponseModel();
        Mockito.when(client.auth(any())).thenReturn(baseResponseModel);
        Mockito.when(client.pay(any())).thenReturn(baseResponseModel);

        //recurrent payment
        paymentContext = MockUtil.buildRecurrentPaymentContext(String.valueOf(new Date().getTime()), token);

        paymentProxyResult = serverHandlerLogDecorator.processPayment(paymentContext);
        assertNotNull(paymentProxyResult.getTrx().getId());
        assertEquals(Step.CHECK_STATUS,
                temporaryContextDeserializer.read(paymentProxyResult.getNextState()).getNextStep());

        paymentProxyResult = processWithDoNothingSuccessResult(paymentContext, paymentProxyResult);

        // captured
        checkSuccessCapture(paymentContext, paymentProxyResult, paymentProxyResult.getNextState());
    }

}
