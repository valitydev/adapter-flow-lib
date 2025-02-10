package dev.vality.adapter.flow.lib.service;

import dev.vality.adapter.common.cds.BankCardExtractor;
import dev.vality.adapter.common.cds.CdsStorageClient;
import dev.vality.adapter.common.cds.model.CardDataProxyModel;
import dev.vality.cds.storage.CardData;
import dev.vality.damsel.domain.BankCard;
import dev.vality.damsel.proxy_provider.PaymentContext;
import dev.vality.damsel.proxy_provider.RecurrentTokenContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CardDataServiceDefaultImpl implements CardDataService {

    private final CdsStorageClient cdsStorageClient;

    @Override
    public CardDataProxyModel getCardDataProxyModel(RecurrentTokenContext context,
                                                    CardData cardData,
                                                    BankCard bankCard) {
        return BankCardExtractor.initCardDataProxyModel(bankCard, cardData);
    }

    @Override
    public CardDataProxyModel getCardDataProxyModel(PaymentContext context, CardData cardData, BankCard bankCard) {
        return BankCardExtractor.initCardDataProxyModel(bankCard, cardData);
    }

    @Override
    public CardDataProxyModel getCardDataProxyModelFromCds(PaymentContext context) {
        return cdsStorageClient.getCardData(context);
    }
}
