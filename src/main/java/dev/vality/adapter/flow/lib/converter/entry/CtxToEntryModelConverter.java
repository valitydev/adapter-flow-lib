package dev.vality.adapter.flow.lib.converter.entry;

import dev.vality.adapter.common.cds.CdsStorageClient;
import dev.vality.adapter.common.cds.model.CardDataProxyModel;
import dev.vality.adapter.common.damsel.ProxyProviderPackageCreators;
import dev.vality.adapter.common.damsel.ProxyProviderPackageExtractors;
import dev.vality.adapter.flow.lib.constant.MetaData;
import dev.vality.adapter.flow.lib.constant.Step;
import dev.vality.adapter.flow.lib.constant.TargetStatus;
import dev.vality.adapter.flow.lib.model.*;
import dev.vality.adapter.flow.lib.serde.TemporaryContextDeserializer;
import dev.vality.adapter.flow.lib.service.CardDataService;
import dev.vality.adapter.flow.lib.service.CardDataServiceWithHolderNamesImpl;
import dev.vality.adapter.flow.lib.service.IdGenerator;
import dev.vality.adapter.flow.lib.service.TemporaryContextService;
import dev.vality.adapter.flow.lib.utils.CallbackUrlExtractor;
import dev.vality.adapter.flow.lib.utils.CardDataUtils;
import dev.vality.adapter.flow.lib.utils.TargetStatusResolver;
import dev.vality.cds.storage.Auth3DS;
import dev.vality.cds.storage.CardData;
import dev.vality.cds.storage.SessionData;
import dev.vality.damsel.domain.BankCard;
import dev.vality.damsel.domain.InvoiceDetails;
import dev.vality.damsel.domain.TransactionInfo;
import dev.vality.damsel.proxy_provider.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
public class CtxToEntryModelConverter implements Converter<PaymentContext, EntryStateModel> {

    private final CdsStorageClient cdsStorageClient;
    private final TemporaryContextDeserializer temporaryContextDeserializer;
    private final IdGenerator idGenerator;
    private final TemporaryContextService temporaryContextService;
    private final CallbackUrlExtractor callbackUrlExtractor;
    private final CardDataService cardDataService;

    @Override
    public EntryStateModel convert(PaymentContext context) {
        var paymentInfo = context.getPaymentInfo();
        var payment = paymentInfo.getPayment();
        var temporaryContext = temporaryContextService.getTemporaryContext(context, temporaryContextDeserializer);
        var paymentResource = payment.getPaymentResource();

        Step currentStep = temporaryContext.getNextStep();
        TargetStatus targetStatus = TargetStatusResolver.extractTargetStatus(context.getSession().getTarget());

        dev.vality.adapter.flow.lib.model.CardData cardData = null;
        MobilePaymentData mobilePaymentData = null;
        if (paymentResource.isSetDisposablePaymentResource()
                && currentStep == null
                && targetStatus == TargetStatus.PROCESSED) {
            SessionData sessionData = cdsStorageClient.getSessionData(context);
            cardData = initCardData(context, paymentResource, sessionData);
            mobilePaymentData = initMobilePaymentData(sessionData);
        }

        TransactionInfo trx = payment.getTrx();
        RecurrentPaymentData recurrentPaymentData = initRecurrentPaymentData(payment, paymentResource, trx);
        Map<String, String> adapterConfigurations = context.getOptions();
        Invoice invoice = paymentInfo.getInvoice();
        InvoiceDetails details = invoice.getDetails();
        String invoiceFormatPaymentId = getInvoiceFormatPaymentId(payment, invoice);
        return EntryStateModel.builder()
                .baseRequestModel(BaseRequestModel.builder().recurrentPaymentData(recurrentPaymentData)
                        .mobilePaymentData(mobilePaymentData)
                        .cardData(cardData)
                        .refundData(initRefundData(paymentInfo))
                        .paymentId(idGenerator.get(invoiceFormatPaymentId))
                        .invoiceFormatPaymentId(invoiceFormatPaymentId)
                        .createdAt(paymentInfo.getPayment().getCreatedAt())
                        .currency(Currency.builder()
                                .symbolicCode(payment.getCost().getCurrency().getSymbolicCode())
                                .numericCode(payment.getCost().getCurrency().getNumericCode())
                                .build()
                        ).amount(payment.getCost().getAmount())
                        .details(Objects.requireNonNullElse(details.getDescription(), details.getProduct()))
                        .payerInfo(PayerInfo.builder()
                                .ip(ProxyProviderPackageCreators.extractIpAddress(context))
                                .build())
                        .adapterConfigurations(adapterConfigurations)
                        .providerTrxId(trx != null ? trx.getId() : temporaryContext.getProviderTrxId())
                        .savedData(trx != null ? trx.getExtra() : new HashMap<>())
                        .successRedirectUrl(getSuccessRedirectUrl(payment, adapterConfigurations))
                        .threeDsDataFromMpiCallback(temporaryContext.getThreeDsData())
                        .build())
                .targetStatus(targetStatus)
                .currentStep(currentStep)
                .startedPollingInfo(temporaryContext.getPollingInfo())
                .build();
    }

    private String getInvoiceFormatPaymentId(InvoicePayment payment, Invoice invoice) {
        return invoice.getId() + "." + payment.getId();
    }

    private String getSuccessRedirectUrl(InvoicePayment payment, Map<String, String> adapterConfigurations) {
        return callbackUrlExtractor.getSuccessRedirectUrl(
                adapterConfigurations,
                payment.isSetPayerSessionInfo()
                        ? payment.getPayerSessionInfo().getRedirectUrl()
                        : null);
    }

    private dev.vality.adapter.flow.lib.model.CardData initCardData(PaymentContext context,
                                                                    PaymentResource paymentResource,
                                                                    SessionData sessionData) {
        var cardDataBuilder = dev.vality.adapter.flow.lib.model.CardData.builder();
        if (!isMobilePay(sessionData)) {
            CardDataProxyModel cardData = getCardData(context, paymentResource);
            cardDataBuilder.cardHolder(cardData.getCardholderName())
                    .pan(cardData.getPan())
                    .cvv2(CardDataUtils.extractCvv2(sessionData))
                    .expYear(cardData.getExpYear())
                    .expMonth(cardData.getExpMonth())
                    .cardToken(ProxyProviderPackageExtractors.extractBankCardToken(paymentResource));
        }
        return cardDataBuilder.build();
    }

    private MobilePaymentData initMobilePaymentData(SessionData sessionData) {
        var mobilePaymentDataBuilder = MobilePaymentData.builder();
        if (isMobilePay(sessionData)) {
            Auth3DS auth3ds = sessionData.getAuthData().getAuth3ds();
            mobilePaymentDataBuilder.cryptogram(auth3ds.getCryptogram())
                    .eci(auth3ds.getEci());
        }
        return mobilePaymentDataBuilder.build();
    }

    private boolean isMobilePay(SessionData sessionData) {
        return sessionData.isSetAuthData() && sessionData.getAuthData().isSetAuth3ds();
    }

    private RefundData initRefundData(PaymentInfo paymentInfo) {
        RefundData refundData = null;
        if (paymentInfo.isSetRefund()) {
            InvoicePaymentRefund refund = paymentInfo.getRefund();
            refundData = RefundData.builder()
                    .id(refund.getId())
                    .amount(refund.getCash().getAmount())
                    .build();
        }
        return refundData;
    }

    private RecurrentPaymentData initRecurrentPaymentData(InvoicePayment payment,
                                                          PaymentResource paymentResource,
                                                          TransactionInfo transactionInfo) {
        var recurrentPaymentDataBuilder = RecurrentPaymentData.builder()
                .makeRecurrent(payment.make_recurrent);
        if (paymentResource.isSetRecurrentPaymentResource()) {
            recurrentPaymentDataBuilder.recToken(paymentResource.getRecurrentPaymentResource().getRecToken());
        } else if (transactionInfo != null && transactionInfo.getExtra() != null
                && transactionInfo.getExtra().containsKey(MetaData.META_REC_TOKEN)) {
            recurrentPaymentDataBuilder.recToken(transactionInfo.getExtra().get(MetaData.META_REC_TOKEN));
        }
        return recurrentPaymentDataBuilder.build();
    }

    private CardDataProxyModel getCardData(PaymentContext context, PaymentResource paymentResource) {
        if (paymentResource.isSetDisposablePaymentResource()) {
            String cardToken = ProxyProviderPackageExtractors.extractBankCardToken(paymentResource);
            CardData cardData = cdsStorageClient.getCardData(cardToken);
            BankCard bankCard = ProxyProviderPackageExtractors.extractBankCard(context);
            return cardDataService.getCardDataProxyModel(context, cardData, bankCard);
        }
        return cardDataService.getCardDataProxyModelFromCds(context);
    }

}

