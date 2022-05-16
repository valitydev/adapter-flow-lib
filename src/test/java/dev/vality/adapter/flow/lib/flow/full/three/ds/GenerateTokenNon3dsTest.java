package dev.vality.adapter.flow.lib.flow.full.three.ds;

import dev.vality.adapter.flow.lib.constant.Step;
import dev.vality.adapter.flow.lib.flow.AbstractGenerateTokenTest;
import dev.vality.adapter.flow.lib.flow.full.three.ds.config.FullThreeDsFlowConfig;
import dev.vality.adapter.flow.lib.flow.utils.BeanUtils;
import dev.vality.adapter.flow.lib.flow.utils.MockUtil;
import dev.vality.adapter.flow.lib.model.BaseResponseModel;
import dev.vality.damsel.proxy_provider.RecurrentTokenContext;
import dev.vality.damsel.proxy_provider.RecurrentTokenProxyResult;
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

import static dev.vality.adapter.flow.lib.flow.full.three.ds.ForwardRecurrentPaymentNon3dsTest.RECURRENT_TOKEN;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = FullThreeDsFlowConfig.class)
@TestPropertySource(properties = {"server.rest.port=8083",
        "error-mapping.file=classpath:fixture/errors.json",
        "service.secret.enabled=true"})
public class GenerateTokenNon3dsTest extends AbstractGenerateTokenTest {

    @BeforeEach
    public void setUp() throws TException {
        MockitoAnnotations.openMocks(this);
        MockUtil.mockAllWithout3Ds(cdsStorageClient, benderClient);

        BaseResponseModel baseResponseModel = BeanUtils.createBaseResponseModel();
        baseResponseModel.setRecurrentToken(RECURRENT_TOKEN);

        Mockito.when(client.auth(any())).thenReturn(baseResponseModel);
        Mockito.when(client.pay(any())).thenReturn(baseResponseModel);
        Mockito.when(client.capture(any())).thenReturn(baseResponseModel);
        Mockito.when(client.refund(any())).thenReturn(baseResponseModel);
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
        RecurrentTokenContext paymentContext = MockUtil.buildRecurrentTokenContext(String.valueOf(new Date().getTime()),
                options);
        RecurrentTokenProxyResult paymentProxyResult = checkSuccessAuthOrPay(paymentContext);

        //capture
        paymentProxyResult = checkSleepWithStatus(Step.REFUND, paymentContext, paymentProxyResult,
                paymentProxyResult.getNextState());

        //refund
        checkSuccessRefund(paymentContext, paymentProxyResult, paymentProxyResult.getNextState());
    }
}
