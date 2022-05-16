package dev.vality.adapter.flow.lib.flow.full.three.ds;

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

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = FullThreeDsFlowConfig.class)
@TestPropertySource(properties = {"server.rest.port=8083",
        "error-mapping.file=classpath:fixture/errors.json",
        "service.secret.enabled=true"})
public class RefundPaymentNon3dsTest extends AbstractPaymentTest {

    @BeforeEach
    public void setUp() throws TException {
        MockitoAnnotations.openMocks(this);
        MockUtil.mockAllWithout3Ds(cdsStorageClient, benderClient);

        BaseResponseModel baseResponseModel = BeanUtils.createBaseResponseModel();
        Mockito.when(client.auth(any())).thenReturn(baseResponseModel);
        Mockito.when(client.pay(any())).thenReturn(baseResponseModel);
        Mockito.when(client.refund(any())).thenReturn(baseResponseModel);
    }

    @Test
    public void testOneStage() throws TException {
        testRefund(MockUtil.buildOptionsOneStage());
    }

    @Test
    public void testTwoStage() throws TException {
        testRefund(MockUtil.buildOptionsOneStage());
    }

    public void testRefund(Map<String, String> options) throws TException {

        // auth
        PaymentContext paymentContext = MockUtil.buildPaymentContext(String.valueOf(new Date().getTime()), options);
        PaymentProxyResult paymentProxyResult = processWithDoNothingSuccessResult(paymentContext);

        //capture
        PaymentProxyResult paymentProxyResultDeposit =
                checkSuccessCapture(paymentContext, paymentProxyResult, new byte[] {});

        //refund
        checkSuccessRefund(1100L, paymentContext, paymentProxyResultDeposit);
    }

}
