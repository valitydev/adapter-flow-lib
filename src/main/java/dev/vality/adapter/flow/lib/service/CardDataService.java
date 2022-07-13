package dev.vality.adapter.flow.lib.service;

import dev.vality.adapter.common.cds.model.CardDataProxyModel;
import dev.vality.cds.storage.CardData;
import dev.vality.damsel.domain.BankCard;
import dev.vality.damsel.proxy_provider.PaymentContext;
import dev.vality.damsel.proxy_provider.RecurrentTokenContext;


public interface CardDataService {

    CardDataProxyModel getCardDataProxyModel(RecurrentTokenContext context,
                                             CardData cardData,
                                             BankCard bankCard);

    CardDataProxyModel getCardDataProxyModel(PaymentContext context, CardData cardData, BankCard bankCard);

    CardDataProxyModel getCardDataProxyModelFromCds(PaymentContext context);

}
