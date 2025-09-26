package dev.vality.adapter.flow.lib.service;

import dev.vality.adapter.common.cds.model.CardDataProxyModel;
import dev.vality.cds.storage.CardData;
import dev.vality.damsel.domain.BankCard;
import dev.vality.damsel.proxy_provider.PaymentContext;


public interface CardDataService {

    CardDataProxyModel getCardDataProxyModel(PaymentContext context, CardData cardData, BankCard bankCard);

    CardDataProxyModel getCardDataProxyModelFromCds(PaymentContext context);

}
