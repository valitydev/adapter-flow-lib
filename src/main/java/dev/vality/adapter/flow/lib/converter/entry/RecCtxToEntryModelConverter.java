package dev.vality.adapter.flow.lib.converter.entry;

import dev.vality.adapter.common.utils.converter.CardDataUtils;
import dev.vality.adapter.flow.lib.constant.MetaData;
import dev.vality.adapter.flow.lib.model.*;
import dev.vality.adapter.flow.lib.service.IdGenerator;
import dev.vality.adapter.flow.lib.utils.AdapterDeserializer;
import dev.vality.adapter.flow.lib.utils.AdapterStateUtils;
import dev.vality.cds.client.storage.CdsClientStorage;
import dev.vality.cds.client.storage.utils.BankCardExtractor;
import dev.vality.cds.storage.Auth3DS;
import dev.vality.cds.storage.CardData;
import dev.vality.cds.storage.SessionData;
import dev.vality.damsel.domain.BankCard;
import dev.vality.damsel.domain.DisposablePaymentResource;
import dev.vality.damsel.domain.PaymentTool;
import dev.vality.damsel.domain.TransactionInfo;
import dev.vality.damsel.proxy_provider.RecurrentPaymentTool;
import dev.vality.damsel.proxy_provider.RecurrentTokenContext;
import dev.vality.damsel.proxy_provider.RecurrentTokenInfo;
import dev.vality.java.cds.utils.model.CardDataProxyModel;
import dev.vality.java.damsel.utils.creators.ProxyProviderPackageCreators;
import dev.vality.java.damsel.utils.extractors.ProxyProviderPackageExtractors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecCtxToEntryModelConverter implements Converter<RecurrentTokenContext, EntryStateModel> {

    private final AdapterDeserializer adapterDeserializer;
    private final CdsClientStorage cdsStorage;
    private final IdGenerator idGenerator;

    @Override
    public EntryStateModel convert(RecurrentTokenContext context) {
        TemporaryContext generalExitStateModel =
                AdapterStateUtils.getTemporaryContext(context, adapterDeserializer);
        RecurrentTokenInfo tokenInfo = context.getTokenInfo();
        RecurrentPaymentTool recurrentPaymentTool = tokenInfo.getPaymentTool();
        DisposablePaymentResource paymentResource = recurrentPaymentTool.getPaymentResource();
        PaymentTool paymentTool = paymentResource.getPaymentTool();
        if (!paymentTool.isSetBankCard()) {
            throw new IllegalArgumentException("Wrong recurrentPaymentTool. It should be bank card");
        }
        TransactionInfo transactionInfo = tokenInfo.getTrx();
        EntryStateModel.EntryStateModelBuilder entryStateModelBuilder =
                EntryStateModel.builder();
        String orderId = idGenerator.get(UUID.randomUUID().toString()).toString();

        MobilePaymentData.MobilePaymentDataBuilder mobilePaymentDataBuilder = MobilePaymentData.builder();
        dev.vality.adapter.flow.lib.model.CardData.CardDataBuilder cardDataBuilder =
                dev.vality.adapter.flow.lib.model.CardData.builder();
        if (generalExitStateModel == null) {
            SessionData sessionData = cdsStorage.getSessionData(context);
            if (sessionData.getAuthData().isSetAuth3ds()) {
                Auth3DS auth3ds = sessionData.getAuthData().getAuth3ds();
                mobilePaymentDataBuilder.cryptogram(auth3ds.getCryptogram())
                        .eci(auth3ds.getEci());
            } else {
                CardDataProxyModel cardData = getCardData(context, paymentResource);
                cardDataBuilder.cardHolder(cardData.getCardholderName())
                        .pan(cardData.getPan())
                        .cvv2(CardDataUtils.extractCvv2(sessionData))
                        .expYear(cardData.getExpYear())
                        .expMonth(cardData.getExpMonth());
            }
        }

        return entryStateModelBuilder
                .baseRequestModel(BaseRequestModel.builder()
                        .recurrentPaymentData(RecurrentPaymentData
                                .builder()
                                .makeRecurrent(true)
                                .recToken(transactionInfo != null && transactionInfo.getExtra() != null
                                        ? transactionInfo.getExtra().get(MetaData.META_REC_TOKEN)
                                        : null)
                                .build())
                        .mobilePaymentData(mobilePaymentDataBuilder.build())
                        .cardData(cardDataBuilder.build())
                        .refundData(initRefundData(recurrentPaymentTool, orderId))
                        .paymentId(orderId)
                        .currency(recurrentPaymentTool.getMinimalPaymentCost().getCurrency().getSymbolicCode())
                        .amount(recurrentPaymentTool.getMinimalPaymentCost().getAmount())
                        .details(recurrentPaymentTool.getId())
                        .payerInfo(PayerInfo.builder()
                                .ip(ProxyProviderPackageCreators.extractIpAddress(context))
                                .build())
                        .adapterConfigurations(context.getOptions())
                        .providerTrxId(transactionInfo != null ? transactionInfo.getId() : null)
                        .savedData(transactionInfo == null || transactionInfo.getExtra() == null
                                ? new HashMap<>()
                                : transactionInfo.getExtra())
                        .build())
                .currentStep(generalExitStateModel.getNextStep())
                .build();
    }

    private RefundData initRefundData(RecurrentPaymentTool recurrentPaymentTool, String orderId) {
        return RefundData.builder()
                .id(orderId)
                .amount(recurrentPaymentTool.getMinimalPaymentCost().getAmount())
                .build();
    }

    private CardDataProxyModel getCardData(RecurrentTokenContext context, DisposablePaymentResource paymentResource) {
        String cardToken = ProxyProviderPackageExtractors.extractBankCardToken(paymentResource);
        CardData cardData = cdsStorage.getCardData(cardToken);
        BankCard bankCard = ProxyProviderPackageExtractors.extractBankCard(context);
        return BankCardExtractor.initCardDataProxyModel(bankCard, cardData);
    }
}
