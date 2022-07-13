package dev.vality.adapter.flow.lib.service;

import dev.vality.adapter.common.cds.BankCardExtractor;
import dev.vality.adapter.common.cds.CdsStorageClient;
import dev.vality.adapter.common.cds.model.CardDataProxyModel;
import dev.vality.adapter.flow.lib.constant.OptionFields;
import dev.vality.cds.storage.CardData;
import dev.vality.damsel.domain.BankCard;
import dev.vality.damsel.proxy_provider.PaymentContext;
import dev.vality.damsel.proxy_provider.RecurrentTokenContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CardDataServiceWithHolderNamesImpl implements CardDataService {

    private final CdsStorageClient cdsStorageClient;
    private final CardHolderNamesService cardHolderNamesService;

    @Override
    public CardDataProxyModel getCardDataProxyModel(RecurrentTokenContext context,
                                                    CardData cardData,
                                                    BankCard bankCard) {
        if (context.getOptions().containsKey(OptionFields.CARD_HOLDER_FROM_FILE.name())) {
            return BankCardExtractor.initCardDataProxyModel(bankCard, cardData,
                    cardHolderNamesService.getCardHoldersNames());
        }
        return BankCardExtractor.initCardDataProxyModel(bankCard, cardData);
    }

    @Override
    public CardDataProxyModel getCardDataProxyModel(PaymentContext context, CardData cardData, BankCard bankCard) {
        if (context.getOptions().containsKey(OptionFields.CARD_HOLDER_FROM_FILE.name())) {
            return BankCardExtractor.initCardDataProxyModel(bankCard, cardData,
                    cardHolderNamesService.getCardHoldersNames());
        }
        return BankCardExtractor.initCardDataProxyModel(bankCard, cardData);
    }

    @Override
    public CardDataProxyModel getCardDataProxyModelFromCds(PaymentContext context) {
        if (context.getOptions().containsKey(OptionFields.CARD_HOLDER_FROM_FILE.name())) {
            return cdsStorageClient.getCardDataWithListHolders(context, cardHolderNamesService.getCardHoldersNames());
        }
        return cdsStorageClient.getCardData(context);
    }
}
