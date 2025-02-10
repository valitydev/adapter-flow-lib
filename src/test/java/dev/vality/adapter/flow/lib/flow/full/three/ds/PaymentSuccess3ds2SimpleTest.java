package dev.vality.adapter.flow.lib.flow.full.three.ds;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.vality.adapter.flow.lib.constant.Status;
import dev.vality.adapter.flow.lib.constant.Step;
import dev.vality.adapter.flow.lib.flow.AbstractPaymentTest;
import dev.vality.adapter.flow.lib.flow.full.three.ds.config.FullThreeDsFlowConfig;
import dev.vality.adapter.flow.lib.flow.utils.BeanUtils;
import dev.vality.adapter.flow.lib.flow.utils.MockUtil;
import dev.vality.adapter.flow.lib.model.BaseResponseModel;
import dev.vality.damsel.proxy_provider.PaymentCallbackResult;
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
import java.nio.ByteBuffer;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

@Slf4j
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = FullThreeDsFlowConfig.class)
@TestPropertySource(properties = {"error-mapping.file=classpath:fixture/errors.json",
        "adapter.callbackUrl=http://localhost:8080/test",
        "server.rest.endpoint=adapter",
        "server.rest.port=8083"})
public class PaymentSuccess3ds2SimpleTest extends AbstractPaymentTest {

    @BeforeEach
    public void setUp() throws TException {
        MockitoAnnotations.openMocks(this);

        MockUtil.mock3ds1CardData(cdsStorageClient);
        MockUtil.mock3ds1SessionData(cdsStorageClient);
        MockUtil.mockIdGenerator(benderClient);

        BaseResponseModel baseResponseModelCheck = BeanUtils.createBaseResponseModel();
        baseResponseModelCheck.setThreeDsData(BeanUtils.create3Ds2FullCheck());
        baseResponseModelCheck.setStatus(Status.NEED_REDIRECT);

        BaseResponseModel baseResponseModel = BeanUtils.createBaseResponseModel();

        Mockito.when(client.auth(any())).thenReturn(baseResponseModelCheck);
        Mockito.when(client.pay(any())).thenReturn(baseResponseModelCheck);
        Mockito.when(client.capture(any())).thenReturn(baseResponseModel);
        Mockito.when(client.check3dsV2(any())).thenReturn(baseResponseModel);
        Mockito.when(client.refund(any())).thenReturn(baseResponseModel);
    }

    @Test
    public void testOneStage() throws TException, IOException {
        Map<String, String> options = MockUtil.buildOptionsOneStage();
        test3ds2Full(options);
    }

    @Test
    public void testTwoStage() throws TException, IOException {
        Map<String, String> options = MockUtil.buildOptionsTwoStage();
        test3ds2Full(options);
    }

    private void test3ds2Full(Map<String, String> options) throws TException, JsonProcessingException {
        // auth
        PaymentContext paymentContext = MockUtil.buildPaymentContext("invoice_id", options);

        PaymentProxyResult paymentProxyResult = serverHandlerLogDecorator.processPayment(paymentContext);
        assertTrue(paymentProxyResult.getIntent().getSuspend().getUserInteraction().isSetRedirect());
        assertEquals(Step.CHECK_NEED_3DS_V2,
                temporaryContextDeserializer.read(paymentProxyResult.getNextState()).getNextStep());

        BaseResponseModel baseResponseModel = BeanUtils.createBaseResponseModel();
        baseResponseModel.setThreeDsData(null);
        Mockito.when(client.check3dsV2(any())).thenReturn(baseResponseModel);

        ByteBuffer byteBuffer = BeanUtils.createSessionBuffer("cres", "threeDSMethodData");
        paymentContext.getPaymentInfo().getPayment().setTrx(paymentProxyResult.getTrx());
        paymentContext.getSession().setState(paymentProxyResult.getNextState());
        PaymentCallbackResult paymentCallbackResult = serverHandlerLogDecorator.handlePaymentCallback(byteBuffer,
                paymentContext);

        //finish three ds
        paymentProxyResult = checkSuccessFinishThreeDs(paymentContext, paymentProxyResult, paymentCallbackResult);

        //capture
        PaymentProxyResult paymentProxyResultDeposit =
                checkSuccessCapture(paymentContext, paymentProxyResult, new byte[] {});

        //refund
        checkSuccessRefund(1100L, paymentContext, paymentProxyResultDeposit);
    }

}
