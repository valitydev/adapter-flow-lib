package dev.vality.adapter.flow.lib.flow.full.three.ds;

import dev.vality.adapter.flow.lib.constant.Step;
import dev.vality.adapter.flow.lib.flow.AbstractPaymentTest;
import dev.vality.adapter.flow.lib.flow.full.three.ds.config.FullThreeDsFlowConfig;
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
@ContextConfiguration(classes = FullThreeDsFlowConfig.class)
@TestPropertySource(properties = {"server.rest.port=8083",
        "error-mapping.file=classpath:fixture/errors.json",
        "service.secret.enabled=true"})
public class ForwardRecurrentPaymentNon3dsTest extends AbstractPaymentTest {

    public static final String RECURRENT_TOKEN = "recurrentToken";

    @BeforeEach
    public void setUp() throws TException {
        MockitoAnnotations.openMocks(this);
        MockUtil.mockAllWithout3Ds(cdsStorageClient, benderClient);

        BaseResponseModel baseResponseModel = BeanUtils.createBaseResponseModel();
        baseResponseModel.setRecurrentToken(RECURRENT_TOKEN);

        Mockito.when(client.auth(any())).thenReturn(baseResponseModel);
        Mockito.when(client.pay(any())).thenReturn(baseResponseModel);
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

        PaymentProxyResult paymentProxyResult = processWithDoNothingSuccessResult(paymentContext);
        String token = paymentProxyResult.getIntent().getFinish().getStatus().getSuccess().getToken();

        //capture
        paymentProxyResult = checkSuccessCapture(paymentContext, paymentProxyResult, new byte[] {});
        String tokenCapture = paymentProxyResult.getIntent().getFinish().getStatus().getSuccess().getToken();
        assertEquals(token, tokenCapture);

        //recurrent payment
        paymentContext = MockUtil.buildRecurrentPaymentContext(String.valueOf(new Date().getTime()), token);

        paymentProxyResult = serverHandlerLogDecorator.processPayment(paymentContext);
        assertNotNull(paymentProxyResult.getTrx().getId());
        assertEquals(Step.DO_NOTHING,
                temporaryContextDeserializer.read(paymentProxyResult.getNextState()).getNextStep());

        // captured
        checkSuccessCapture(paymentContext, paymentProxyResult, paymentProxyResult.getNextState());
    }

}
