package dev.vality.adapter.flow.lib.service;

import dev.vality.adapter.common.cds.model.CardDataProxyModel;
import dev.vality.adapter.flow.lib.constant.OptionFields;
import dev.vality.adapter.flow.lib.flow.AbstractPaymentTest;
import dev.vality.adapter.flow.lib.flow.simple.redirect.config.SimpleRedirectWithPollingDsFlowConfig;
import dev.vality.cds.storage.CardData;
import dev.vality.damsel.domain.BankCard;
import dev.vality.damsel.domain.BankCardExpDate;
import dev.vality.damsel.proxy_provider.PaymentContext;
import dev.vality.damsel.proxy_provider.RecurrentTokenContext;
import org.apache.thrift.TException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SimpleRedirectWithPollingDsFlowConfig.class)
@TestPropertySource(properties = {"server.rest.port=8083",
        "error-mapping.file=classpath:fixture/errors.json"})
class CardDataServiceWithHolderNamesImplTest extends AbstractPaymentTest {

    public static final String TEST_1 = "TEST 1";
    @Autowired
    private CardDataServiceWithHolderNamesImpl cardDataServiceWithHolderNames;

    @BeforeEach
    public void setUp() throws TException {
        MockitoAnnotations.openMocks(this);
        doAnswer((Answer<CardDataProxyModel>) invocationOnMock -> CardDataProxyModel.builder()
                .cardholderName(TEST_1)
                .build()).when(cdsStorageClient).getCardDataWithListHolders(any(PaymentContext.class), any());
    }

    @Test
    void getCardDataProxyModel() {
        CardDataProxyModel cardDataProxyModel = cardDataServiceWithHolderNames.getCardDataProxyModel(
                new PaymentContext()
                        .setOptions(Map.of(OptionFields.CARD_HOLDER_FROM_FILE.name(), "true")),
                new CardData(),
                new BankCard()
                        .setToken("test")
                        .setExpDate(new BankCardExpDate()));

        Assertions.assertEquals(TEST_1, cardDataProxyModel.getCardholderName());


        cardDataProxyModel = cardDataServiceWithHolderNames.getCardDataProxyModel(
                new PaymentContext()
                        .setOptions(Map.of()),
                new CardData(),
                new BankCard().setToken("test")
                        .setExpDate(new BankCardExpDate()));

        Assertions.assertNotEquals(TEST_1, cardDataProxyModel.getCardholderName());
    }

    @Test
    void testGetCardDataProxyModel() {
        CardDataProxyModel cardDataProxyModel = cardDataServiceWithHolderNames.getCardDataProxyModel(
                new RecurrentTokenContext()
                        .setOptions(Map.of(OptionFields.CARD_HOLDER_FROM_FILE.name(), "true")),
                new CardData(),
                new BankCard()
                        .setToken("test")
                        .setExpDate(new BankCardExpDate()));

        Assertions.assertEquals(TEST_1, cardDataProxyModel.getCardholderName());

        cardDataProxyModel = cardDataServiceWithHolderNames.getCardDataProxyModel(
                new RecurrentTokenContext()
                        .setOptions(Map.of()),
                new CardData(),
                new BankCard().setToken("test")
                        .setExpDate(new BankCardExpDate()));

        Assertions.assertNotEquals(TEST_1, cardDataProxyModel.getCardholderName());
    }

    @Test
    void getCardDataProxyModelFromCds() {
        CardDataProxyModel cardDataProxyModel = cardDataServiceWithHolderNames.getCardDataProxyModelFromCds(
                new PaymentContext().setOptions(Map.of(OptionFields.CARD_HOLDER_FROM_FILE.name(), "true")));

        Assertions.assertEquals(TEST_1, cardDataProxyModel.getCardholderName());
    }
}