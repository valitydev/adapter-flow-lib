package dev.vality.adapter.flow.lib.converter.entry;

import dev.vality.adapter.flow.lib.constant.MetaData;
import dev.vality.adapter.flow.lib.model.*;
import dev.vality.adapter.flow.lib.serde.TemporaryContextDeserializer;
import dev.vality.adapter.flow.lib.service.IdGenerator;
import dev.vality.adapter.flow.lib.service.TemporaryContextService;
import dev.vality.adapter.flow.lib.utils.CardDataUtils;
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
import dev.vality.java.cds.utils.model.CardDataProxyModel;
import dev.vality.java.damsel.utils.creators.ProxyProviderPackageCreators;
import dev.vality.java.damsel.utils.extractors.ProxyProviderPackageExtractors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;

import java.util.HashMap;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class RecCtxToEntryModelConverter implements Converter<RecurrentTokenContext, EntryStateModel> {

    private final TemporaryContextDeserializer temporaryContextDeserializer;
    private final CdsClientStorage cdsStorage;
    private final IdGenerator idGenerator;
    private final TemporaryContextService temporaryContextService;

    @Override
    public EntryStateModel convert(RecurrentTokenContext context) {
        var generalExitStateModel = temporaryContextService.getTemporaryContext(
                context, temporaryContextDeserializer);
        var tokenInfo = context.getTokenInfo();
        var recurrentPaymentTool = tokenInfo.getPaymentTool();
        var paymentResource = recurrentPaymentTool.getPaymentResource();
        var paymentTool = paymentResource.getPaymentTool();
        validatePaymentTool(paymentTool);
        var entryStateModelBuilder =
                EntryStateModel.builder();
        var mobilePaymentDataBuilder = MobilePaymentData.builder();
        var cardDataBuilder = dev.vality.adapter.flow.lib.model.CardData.builder();
        initPaymentData(context, generalExitStateModel, paymentResource, mobilePaymentDataBuilder, cardDataBuilder);

        TransactionInfo transactionInfo = tokenInfo.getTrx();
        Long orderId = idGenerator.get(tokenInfo.getPaymentTool().getId());
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

    private void initPaymentData(RecurrentTokenContext context, TemporaryContext generalExitStateModel,
                                 DisposablePaymentResource paymentResource,
                                 MobilePaymentData.MobilePaymentDataBuilder<?, ?> mobilePaymentDataBuilder,
                                 dev.vality.adapter.flow.lib.model.CardData.CardDataBuilder<?, ?> cardDataBuilder) {
        if (generalExitStateModel == null || generalExitStateModel.getNextStep() == null) {
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
    }

    private void validatePaymentTool(PaymentTool paymentTool) {
        if (!paymentTool.isSetBankCard()) {
            throw new IllegalArgumentException("Wrong recurrentPaymentTool. It should be bank card");
        }
    }

    private RefundData initRefundData(RecurrentPaymentTool recurrentPaymentTool, Long orderId) {
        return RefundData.builder()
                .id(String.valueOf(orderId))
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
