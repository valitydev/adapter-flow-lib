package dev.vality.adapter.flow.lib.flow.simple.redirect;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.vality.adapter.flow.lib.constant.Status;
import dev.vality.adapter.flow.lib.constant.Step;
import dev.vality.adapter.flow.lib.flow.AbstractGenerateTokenTest;
import dev.vality.adapter.flow.lib.flow.simple.redirect.config.SimpleRedirectWithPollingDsFlowConfig;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SimpleRedirectWithPollingDsFlowConfig.class)
@TestPropertySource(properties = {"server.rest.port=8083",
        "error-mapping.file=classpath:fixture/errors.json",
        "service.secret.enabled=true"})
public class GenerateToken3ds1Test extends AbstractGenerateTokenTest {

    @BeforeEach
    public void setUp() throws TException {
        MockitoAnnotations.openMocks(this);
        MockUtil.mockAllWithout3Ds(cdsClientStorage, benderClient);

        BaseResponseModel baseResponseModel = BeanUtils.createBaseResponseModel();
        baseResponseModel.setThreeDsData(BeanUtils.create3Ds1(baseResponseModel));
        baseResponseModel.setStatus(Status.NEED_REDIRECT);

        BaseResponseModel baseResponseModelRec = BeanUtils.createBaseResponseModel();
        baseResponseModelRec.setRecurrentToken(RECURRENT_TOKEN);

        Mockito.when(client.generateToken(any())).thenReturn(baseResponseModel);
        Mockito.when(client.status(any())).thenReturn(baseResponseModelRec);
    }

    @Test
    public void testPaymentOneStage() throws TException, JsonProcessingException {
        // pay
        Map<String, String> options = MockUtil.buildOptionsOneStage();

        RecurrentTokenContext paymentContext =
                MockUtil.buildRecurrentTokenContext(String.valueOf(new Date().getTime()),
                        options);
        RecurrentTokenProxyResult paymentProxyResult = serverHandlerLogDecorator.generateToken(paymentContext);
        assertTrue(paymentProxyResult.getIntent().getSuspend().getUserInteraction().isSetRedirect());
        assertEquals(Step.CHECK_STATUS,
                temporaryContextDeserializer.read(paymentProxyResult.getNextState()).getNextStep());

        //checkStatus
        paymentProxyResult = processWithDoNothingSuccessResult(paymentContext, paymentProxyResult);
    }

}
