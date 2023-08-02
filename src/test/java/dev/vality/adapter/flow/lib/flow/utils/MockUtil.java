package dev.vality.adapter.flow.lib.flow.utils;

import dev.vality.adapter.common.cds.CdsStorageClient;
import dev.vality.adapter.common.cds.model.CardDataProxyModel;
import dev.vality.adapter.flow.lib.constant.OptionFields;
import dev.vality.adapter.flow.lib.constant.Stage;
import dev.vality.bender.BenderSrv;
import dev.vality.bender.GenerationResult;
import dev.vality.cds.storage.*;
import dev.vality.damsel.domain.*;
import dev.vality.damsel.proxy_provider.Cash;
import dev.vality.damsel.proxy_provider.Invoice;
import dev.vality.damsel.proxy_provider.InvoicePayment;
import dev.vality.damsel.proxy_provider.*;
import org.apache.thrift.TException;
import org.mockito.stubbing.Answer;

import java.util.Date;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

public class MockUtil {

    public static final String PAN_SUCCESS_NON3DS = "5543725660917340";
    public static final String PAN_SUCCESS_3DS_1 = "5543735484626654";
    public static final String PAN_SUCCESS_3DS_2 = "2201382000000047";
    public static final String PAN_SUCCESS_3DS_2_SIMPLE_FLOW = "2201382000000013";

    public static final String CARDHOLDER_NAME = "NONAME";

    public static final int EXP_MONTH_NON3DS = 01;
    public static final int EXP_YEAR_NON3DS = 2022;

    public static final int EXP_MONTH_3DS_2 = 12;
    public static final int EXP_YEAR_3DS_2 = 2025;

    public static final String CVV_NON3DS = "087";
    public static final String CVV_3DS_1 = "852";
    public static final String CVV_3DS_2 = "584";
    public static final String CVV_3DS_2_SIMPLE_FLOW = "590";

    public static void mockAllWithout3Ds(CdsStorageClient cdsStorageClient, BenderSrv.Iface benderClient)
            throws TException {
        MockUtil.mockCardDataWithout3ds(cdsStorageClient);
        MockUtil.mockSessionData(cdsStorageClient);
        MockUtil.mockIdGenerator(benderClient);
    }

    public static void mockCardDataWithout3ds(CdsStorageClient cdsStorageClient) {
        mockCardData(EXP_MONTH_NON3DS,
                EXP_YEAR_NON3DS,
                CARDHOLDER_NAME,
                PAN_SUCCESS_NON3DS,
                cdsStorageClient);
    }

    public static void mock3ds1CardData(CdsStorageClient cdsStorageClient) {
        mockCardData(EXP_MONTH_NON3DS,
                EXP_YEAR_NON3DS,
                CARDHOLDER_NAME,
                PAN_SUCCESS_3DS_1,
                cdsStorageClient);
    }

    public static void mock3ds2FullFlowCardData(CdsStorageClient cdsStorageClient) {
        mockCardData(EXP_MONTH_3DS_2,
                EXP_YEAR_3DS_2,
                CARDHOLDER_NAME,
                PAN_SUCCESS_3DS_2,
                cdsStorageClient);
    }

    public static void mock3ds2SimpleFlowCardData(CdsStorageClient cdsStorageClient) {
        mockCardData(EXP_MONTH_3DS_2,
                EXP_YEAR_3DS_2,
                CARDHOLDER_NAME,
                PAN_SUCCESS_3DS_2_SIMPLE_FLOW,
                cdsStorageClient);
    }

    private static void mockCardData(int expMonthNon3ds,
                                     int expYearNon3ds,
                                     String cardholderName,
                                     String panSuccessNon3ds,
                                     CdsStorageClient cdsStorageClient) {
        doAnswer((Answer<CardDataProxyModel>) invocationOnMock -> CardDataProxyModel.builder()
                .expMonth((byte) expMonthNon3ds)
                .expYear((short) expYearNon3ds)
                .cardholderName(cardholderName)
                .pan(panSuccessNon3ds)
                .build()).when(cdsStorageClient).getCardData(any(RecurrentTokenContext.class));
        doAnswer((Answer<CardData>) invocation ->
                new CardData()
                        .setExpDate(new ExpDate()
                                .setMonth((byte) expMonthNon3ds)
                                .setYear((short) expYearNon3ds))
                        .setPan(panSuccessNon3ds)
                        .setCardholderName(cardholderName))
                .when(cdsStorageClient).getCardData(any(PaymentContext.class));
        doAnswer((Answer<CardData>) invocation ->
                new CardData()
                        .setExpDate(new ExpDate()
                                .setMonth((byte) expMonthNon3ds)
                                .setYear((short) expYearNon3ds))
                        .setPan(panSuccessNon3ds)
                        .setCardholderName(cardholderName))
                .when(cdsStorageClient).getCardData(any(String.class));
    }

    public static void mockSessionData(CdsStorageClient cdsStorageClient) {
        mockCvv(CVV_NON3DS, cdsStorageClient);
    }

    public static void mock3ds1SessionData(CdsStorageClient cdsStorageClient) {
        mockCvv(CVV_3DS_1, cdsStorageClient);
    }

    public static void mock3ds2FullFlowSessionData(CdsStorageClient cdsStorageClient) {
        mockCvv(CVV_3DS_2, cdsStorageClient);
    }

    public static void mock3ds2SimpleFlowSessionData(CdsStorageClient cdsStorageClient) {
        mockCvv(CVV_3DS_2_SIMPLE_FLOW, cdsStorageClient);
    }

    private static void mockCvv(String cvvNon3ds, CdsStorageClient cdsStorageClient) {
        doAnswer((Answer<SessionData>) invocation ->
                new SessionData(AuthData.card_security_code(new CardSecurityCode(cvvNon3ds))))
                .when(cdsStorageClient).getSessionData(any(RecurrentTokenContext.class));
        doAnswer((Answer<SessionData>) invocation ->
                new SessionData(AuthData.card_security_code(new CardSecurityCode(cvvNon3ds))))
                .when(cdsStorageClient).getSessionData(any(PaymentContext.class));
    }

    public static void mockIdGenerator(BenderSrv.Iface benderClient) throws TException {
        doAnswer(invocationOnMock -> new GenerationResult().setInternalId(String.valueOf(new Date().getTime())))
                .when(benderClient).generateID(any(), any(), any());
    }

    public static PaymentContext buildPaymentContext(String invoiceId, Map<String, String> options) {
        return new PaymentContext()
                .setSession(new Session()
                        .setTarget(TargetInvoicePaymentStatus.processed(
                                new InvoicePaymentProcessed())))
                .setPaymentInfo(new PaymentInfo()
                        .setInvoice(new Invoice()
                                .setId(invoiceId)
                                .setDetails(new InvoiceDetails()
                                        .setDescription("details")))
                        .setPayment(new InvoicePayment()
                                .setId("payment_id")
                                .setCreatedAt("2016-03-22T06:12:27Z")
                                .setPaymentResource(PaymentResource.disposable_payment_resource(
                                        new DisposablePaymentResource()
                                                .setClientInfo(new ClientInfo()
                                                        .setIpAddress("185.31.132.50"))
                                                .setPaymentTool(buildPaymentTool())))
                                .setCost(new Cash()
                                        .setAmount(1200)
                                        .setCurrency(new Currency()
                                                .setSymbolicCode("RUB")
                                                .setNumericCode((short) 643)))
                                .setContactInfo(new ContactInfo()
                                        .setEmail("kkkk@kkk.ru")
                                        .setPhoneNumber("89037772299"))
                        )
                )

                .setOptions(options);
    }

    public static PaymentContext buildPaymentContextPaymentTerminal(String invoiceId, Map<String, String> options) {
        return new PaymentContext()
                .setSession(new Session()
                        .setTarget(TargetInvoicePaymentStatus.processed(
                                new InvoicePaymentProcessed())))
                .setPaymentInfo(new PaymentInfo()
                        .setInvoice(new Invoice()
                                .setId(invoiceId)
                                .setDetails(new InvoiceDetails()
                                        .setDescription("details")))
                        .setPayment(new InvoicePayment()
                                .setId("payment_id")
                                .setCreatedAt("2016-03-22T06:12:27Z")
                                .setPaymentResource(PaymentResource.disposable_payment_resource(
                                        new DisposablePaymentResource()
                                                .setClientInfo(new ClientInfo()
                                                        .setIpAddress("185.31.132.50"))
                                                .setPaymentTool(buildPaymentToolPaymentTerminal())))
                                .setCost(new Cash()
                                        .setAmount(1200)
                                        .setCurrency(new Currency()
                                                .setSymbolicCode("RUB")
                                                .setNumericCode((short) 643)))
                                .setContactInfo(new ContactInfo()
                                        .setEmail("kkkk@kkk.ru")
                                        .setPhoneNumber("89037772299"))
                        )
                )

                .setOptions(options);
    }

    public static Map<String, String> buildOptionsOneStage() {
        return Map.of(OptionFields.STAGE.name(), Stage.ONE);
    }

    public static Map<String, String> buildOptionsTwoStage() {
        return Map.of();
    }

    public static PaymentTool buildPaymentTool() {
        return PaymentTool.bank_card(
                new BankCard()
                        .setToken("kektoken")
                        .setBin("1234")
                        .setExpDate(new BankCardExpDate()
                                .setMonth((byte) EXP_MONTH_NON3DS)
                                .setYear((short) EXP_YEAR_NON3DS))
        );
    }

    public static PaymentTool buildPaymentToolPaymentTerminal() {
        return PaymentTool.payment_terminal(
                new PaymentTerminal()
        );
    }

    public static PaymentContext buildRecurrentPaymentContext(String invoiceId, String token) {
        PaymentContext paymentContext = MockUtil.buildPaymentContext(invoiceId,
                MockUtil.buildOptionsOneStage());
        paymentContext.getPaymentInfo().getPayment()
                .setPaymentResource(PaymentResource.recurrent_payment_resource(new RecurrentPaymentResource()
                        .setPaymentTool(MockUtil.buildPaymentTool())
                        .setRecToken(token)));
        return paymentContext;
    }

    public static RecurrentTokenContext buildRecurrentTokenContext(String recurrentId, Map<String, String> options) {
        return new RecurrentTokenContext()
                .setSession(new RecurrentTokenSession())
                .setTokenInfo(new RecurrentTokenInfo()
                        .setPaymentTool(new RecurrentPaymentTool()
                                .setId(recurrentId)
                                .setCreatedAt("2016-03-22T06:12:27Z")
                                .setPaymentResource(new DisposablePaymentResource()
                                        .setPaymentTool(PaymentTool.bank_card(new BankCard()
                                                .setToken("kektoken")
                                                .setBin("1234")
                                                .setExpDate(new BankCardExpDate()
                                                        .setMonth((byte) EXP_MONTH_NON3DS)
                                                        .setYear((short) EXP_YEAR_NON3DS)))))
                                .setMinimalPaymentCost(new Cash()
                                        .setAmount(1000)
                                        .setCurrency(new Currency()
                                                .setSymbolicCode("RUB")
                                                .setNumericCode((short) 643)))))
                .setOptions(options);
    }
}
