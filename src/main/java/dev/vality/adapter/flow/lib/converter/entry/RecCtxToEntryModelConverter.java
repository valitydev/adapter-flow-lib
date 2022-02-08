package dev.vality.adapter.flow.lib.converter.entry;

import dev.vality.adapter.flow.lib.model.GeneralEntryStateModel;
import dev.vality.adapter.flow.lib.service.IdGenerator;
import dev.vality.adapter.flow.lib.utils.AdapterDeserializer;
import dev.vality.adapter.flow.lib.utils.AdapterProperties;
import dev.vality.cds.client.storage.CdsClientStorage;
import dev.vality.cds.client.storage.utils.BankCardExtractor;
import dev.vality.cds.storage.CardData;
import dev.vality.damsel.domain.BankCard;
import dev.vality.damsel.domain.DisposablePaymentResource;
import dev.vality.damsel.proxy_provider.RecurrentTokenContext;
import dev.vality.java.cds.utils.model.CardDataProxyModel;
import dev.vality.java.damsel.utils.extractors.ProxyProviderPackageExtractors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecCtxToEntryModelConverter implements Converter<RecurrentTokenContext, GeneralEntryStateModel> {

    private final AdapterDeserializer adapterDeserializer;
    private final CdsClientStorage cdsStorage;
    private final AdapterProperties adapterProperties;
    private final IdGenerator idGenerator;

    @Override
    public GeneralEntryStateModel convert(RecurrentTokenContext context) {
//        AdapterContext adapterContext = AdapterStateUtils.getAdapterContext(context, adapterDeserializer);
//        RecurrentTokenInfo tokenInfo = context.getTokenInfo();
//        RecurrentPaymentTool recurrentPaymentTool = tokenInfo.getPaymentTool();
//        DisposablePaymentResource paymentResource = recurrentPaymentTool.getPaymentResource();
//        PaymentTool paymentTool = paymentResource.getPaymentTool();
//        if (!paymentTool.isSetBankCard()) {
//            throw new IllegalArgumentException("Wrong recurrentPaymentTool. It should be bank card");
//        }
//        TransactionInfo transactionInfo = tokenInfo.getTrx();
//        GeneralEntryStateModel.GeneralEntryStateModelBuilder entryStateModelBuilder =
//        GeneralEntryStateModel.builder();
//        String orderId = idGenerator.get(UUID.randomUUID().toString()).toString();
//
//        MobilePaymentData.MobilePaymentDataBuilder mobilePaymentDataBuilder = MobilePaymentData.builder();
//        dev.vality.adapter.flow.lib.model.CardData.CardDataBuilder cardDataBuilder =
//                dev.vality.adapter.flow.lib.model.CardData.builder();
//        if (adapterContext == null) {
//            SessionData sessionData = cdsStorage.getSessionData(context);
//            if (sessionData.getAuthData().isSetAuth3ds()) {
//                Auth3DS auth3ds = sessionData.getAuthData().getAuth3ds();
//                mobilePaymentDataBuilder.cryptogram(auth3ds.getCryptogram())
//                        .eci(auth3ds.getEci());
//            } else {
//                CardDataProxyModel cardData = getCardData(context, paymentResource);
//                cardDataBuilder.cardHolder(cardData.getCardholderName())
//                        .pan(cardData.getPan())
//                        .cvv2(CardDataUtils.extractCvv2(sessionData))
//                        .expYear(cardData.getExpYear())
//                        .expMonth(cardData.getExpMonth());
//            }
//        }
//
//        return entryStateModelBuilder
//                .baseRequestModel(BaseRequestModel.builder()
//                        .orderId(orderId)
//                        .amount(recurrentPaymentTool.getMinimalPaymentCost().getAmount())
//                        .refundAmount(recurrentPaymentTool.getMinimalPaymentCost().getAmount())
//                        .currencySymbolCode(
//                                recurrentPaymentTool.getMinimalPaymentCost().getCurrency().getSymbolicCode())
//                        .ip(ProxyProviderPackageCreators.extractIpAddress(context))
//                        .trxId(transactionInfo != null ? transactionInfo.getId() : null)
//                        .trxExtra(transactionInfo == null || transactionInfo.getExtra() == null
//                                ? new HashMap<>()
//                                : transactionInfo.getExtra())
//                        .options(context.getOptions())
//                        .makeRecurrent(true)
//                        .callbackUrl(context.getOptions().getOrDefault(
//                                TERM_URL,
//                                adapterProperties.getDefaultTermUrl()))
//                        .invoiceDetails(recurrentPaymentTool.getId())
//                        .build();
        return null;
    }

    private CardDataProxyModel getCardData(RecurrentTokenContext context, DisposablePaymentResource paymentResource) {
        String cardToken = ProxyProviderPackageExtractors.extractBankCardToken(paymentResource);
        CardData cardData = cdsStorage.getCardData(cardToken);
        BankCard bankCard = ProxyProviderPackageExtractors.extractBankCard(context);
        return BankCardExtractor.initCardDataProxyModel(bankCard, cardData);
    }
}
