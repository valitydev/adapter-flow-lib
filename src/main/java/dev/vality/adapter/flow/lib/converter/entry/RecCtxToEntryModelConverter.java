package dev.vality.adapter.flow.lib.converter.entry;

import dev.vality.adapter.common.cds.CdsStorageClient;
import dev.vality.adapter.common.cds.model.CardDataProxyModel;
import dev.vality.adapter.common.damsel.ProxyProviderPackageCreators;
import dev.vality.adapter.common.damsel.ProxyProviderPackageExtractors;
import dev.vality.adapter.flow.lib.constant.MetaData;
import dev.vality.adapter.flow.lib.model.*;
import dev.vality.adapter.flow.lib.serde.TemporaryContextDeserializer;
import dev.vality.adapter.flow.lib.service.CardDataService;
import dev.vality.adapter.flow.lib.service.IdGenerator;
import dev.vality.adapter.flow.lib.service.TemporaryContextService;
import dev.vality.adapter.flow.lib.utils.CardDataUtils;
import dev.vality.cds.storage.Auth3DS;
import dev.vality.cds.storage.CardData;
import dev.vality.cds.storage.SessionData;
import dev.vality.damsel.domain.BankCard;
import dev.vality.damsel.domain.DisposablePaymentResource;
import dev.vality.damsel.domain.PaymentTool;
import dev.vality.damsel.domain.TransactionInfo;
import dev.vality.damsel.proxy_provider.RecurrentPaymentTool;
import dev.vality.damsel.proxy_provider.RecurrentTokenContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;

import java.util.HashMap;

@Slf4j
@RequiredArgsConstructor
public class RecCtxToEntryModelConverter implements Converter<RecurrentTokenContext, EntryStateModel> {

    private final TemporaryContextDeserializer temporaryContextDeserializer;
    private final CdsStorageClient cdsStorageClient;
    private final IdGenerator idGenerator;
    private final TemporaryContextService temporaryContextService;
    private final CardDataService cardDataService;

    @Override
    public EntryStateModel convert(RecurrentTokenContext context) {
        var generalExitStateModel = temporaryContextService.getTemporaryContext(
                context, temporaryContextDeserializer);
        var tokenInfo = context.getTokenInfo();
        var recurrentPaymentTool = tokenInfo.getPaymentTool();
        var paymentResource = recurrentPaymentTool.getPaymentResource();
        var paymentTool = paymentResource.getPaymentTool();
        validatePaymentTool(paymentTool);
        var entryStateModelBuilder = EntryStateModel.builder();
        var cardData = initCardData(context, generalExitStateModel, paymentResource);
        var mobilePaymentData = initMobilePaymentData(context, generalExitStateModel);

        TransactionInfo transactionInfo = tokenInfo.getTrx();
        String invoiceFormatPaymentId = tokenInfo.getPaymentTool().getId();
        Long orderId = idGenerator.get(invoiceFormatPaymentId);
        var temporaryContext = temporaryContextService.getTemporaryContext(context, temporaryContextDeserializer);

        return entryStateModelBuilder
                .baseRequestModel(BaseRequestModel.builder()
                        .recurrentPaymentData(RecurrentPaymentData
                                .builder()
                                .makeRecurrent(true)
                                .recToken(transactionInfo != null && transactionInfo.getExtra() != null
                                        ? transactionInfo.getExtra().get(MetaData.META_REC_TOKEN)
                                        : null)
                                .build())
                        .mobilePaymentData(mobilePaymentData)
                        .cardData(cardData)
                        .refundData(initRefundData(recurrentPaymentTool, orderId))
                        .paymentId(orderId)
                        .invoiceFormatPaymentId(invoiceFormatPaymentId)
                        .currency(Currency.builder()
                                .name(recurrentPaymentTool.getMinimalPaymentCost().getCurrency().getName())
                                .symbolicCode(
                                        recurrentPaymentTool.getMinimalPaymentCost().getCurrency().getSymbolicCode())
                                .numericCode(
                                        recurrentPaymentTool.getMinimalPaymentCost().getCurrency().getNumericCode())
                                .exponent(recurrentPaymentTool.getMinimalPaymentCost().getCurrency().getExponent())
                                .build()
                        )
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
                        .threeDsDataFromMpiCallback(temporaryContext.getThreeDsData())
                        .build())
                .currentStep(generalExitStateModel.getNextStep())
                .startedPollingInfo(temporaryContext.getPollingInfo())
                .build();
    }

    private dev.vality.adapter.flow.lib.model.CardData initCardData(RecurrentTokenContext context,
                                                                    TemporaryContext generalExitStateModel,
                                                                    DisposablePaymentResource paymentResource) {
        var cardDataBuilder = dev.vality.adapter.flow.lib.model.CardData.builder();
        if (generalExitStateModel == null || generalExitStateModel.getNextStep() == null) {
            SessionData sessionData = cdsStorageClient.getSessionData(context);
            if (!sessionData.getAuthData().isSetAuth3ds()) {
                CardDataProxyModel cardData = getCardData(context, paymentResource);
                cardDataBuilder.cardHolder(cardData.getCardholderName())
                        .pan(cardData.getPan())
                        .cvv2(CardDataUtils.extractCvv2(sessionData))
                        .expYear(cardData.getExpYear())
                        .expMonth(cardData.getExpMonth())
                        .cardToken(ProxyProviderPackageExtractors.extractBankCardToken(paymentResource));
            }
        }
        return cardDataBuilder.build();
    }

    private MobilePaymentData initMobilePaymentData(RecurrentTokenContext context,
                                                    TemporaryContext generalExitStateModel) {
        MobilePaymentData.MobilePaymentDataBuilder<?, ?> mobilePaymentDataBuilder = MobilePaymentData.builder();
        if (generalExitStateModel == null || generalExitStateModel.getNextStep() == null) {
            SessionData sessionData = cdsStorageClient.getSessionData(context);
            if (sessionData.getAuthData().isSetAuth3ds()) {
                Auth3DS auth3ds = sessionData.getAuthData().getAuth3ds();
                mobilePaymentDataBuilder.cryptogram(auth3ds.getCryptogram())
                        .eci(auth3ds.getEci());
            }
        }
        return mobilePaymentDataBuilder.build();
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
        CardData cardData = cdsStorageClient.getCardData(cardToken);
        BankCard bankCard = ProxyProviderPackageExtractors.extractBankCard(context);
        return cardDataService.getCardDataProxyModel(context, cardData, bankCard);
    }

}
