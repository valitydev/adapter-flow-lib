package dev.vality.adapter.flow.lib.converter.entry;

import dev.vality.adapter.common.cds.CdsStorageClient;
import dev.vality.adapter.common.cds.model.CardDataProxyModel;
import dev.vality.adapter.common.damsel.ProxyProviderPackageCreators;
import dev.vality.adapter.common.damsel.ProxyProviderPackageExtractors;
import dev.vality.adapter.flow.lib.constant.MetaData;
import dev.vality.adapter.flow.lib.constant.OptionFields;
import dev.vality.adapter.flow.lib.constant.Step;
import dev.vality.adapter.flow.lib.constant.TargetStatus;
import dev.vality.adapter.flow.lib.model.*;
import dev.vality.adapter.flow.lib.serde.TemporaryContextDeserializer;
import dev.vality.adapter.flow.lib.service.CallbackUrlExtractor;
import dev.vality.adapter.flow.lib.service.CardDataService;
import dev.vality.adapter.flow.lib.service.IdGenerator;
import dev.vality.adapter.flow.lib.service.TemporaryContextService;
import dev.vality.adapter.flow.lib.utils.AdapterProperties;
import dev.vality.adapter.flow.lib.utils.CardDataUtils;
import dev.vality.adapter.flow.lib.utils.TargetStatusResolver;
import dev.vality.cds.storage.Auth3DS;
import dev.vality.cds.storage.CardData;
import dev.vality.cds.storage.SessionData;
import dev.vality.damsel.domain.AdditionalTransactionInfo;
import dev.vality.damsel.domain.BankCard;
import dev.vality.damsel.domain.InvoiceDetails;
import dev.vality.damsel.domain.TransactionInfo;
import dev.vality.damsel.proxy_provider.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.util.StringUtils;

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
    private final AdapterProperties adapterProperties;

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
                && targetStatus == TargetStatus.PROCESSED
                && paymentResource.getDisposablePaymentResource().getPaymentTool().isSetBankCard()) {
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
                                .name(payment.getCost().getCurrency().getName())
                                .symbolicCode(payment.getCost().getCurrency().getSymbolicCode())
                                .numericCode(payment.getCost().getCurrency().getNumericCode())
                                .exponent(payment.getCost().getCurrency().getExponent())
                                .build()
                        ).amount(payment.getCost().getAmount())
                        .details(Objects.requireNonNullElse(details.getDescription(), details.getProduct()))
                        .payerInfo(PayerInfo.builder()
                                .ip(ProxyProviderPackageCreators.extractIpAddress(context))
                                .email(payment.getContactInfo().getEmail())
                                .phone(payment.getContactInfo().getPhoneNumber())
                                .firstName(payment.getContactInfo().getFirstName())
                                .lastName(payment.getContactInfo().getLastName())
                                .country(payment.getContactInfo().getCountry())
                                .state(payment.getContactInfo().getState())
                                .city(payment.getContactInfo().getCity())
                                .address(payment.getContactInfo().getAddress())
                                .postalCode(payment.getContactInfo().getPostalCode())
                                .dateOfBirth(payment.getContactInfo().getDateOfBirth())
                                .documentId(payment.getContactInfo().getDocumentId())
                                .build())
                        .adapterConfigurations(adapterConfigurations)
                        .providerTrxId(trx != null ? trx.getId() : temporaryContext.getProviderTrxId())
                        .savedData(trx != null ? trx.getExtra() : new HashMap<>())
                        .additionalTrxInfo(getAdditionalTrxInfo(context))
                        .successRedirectUrl(getSuccessRedirectUrl(payment, adapterConfigurations))
                        .failedRedirectUrl(getFailureRedirectUrl(payment, adapterConfigurations))
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

    private String getFailureRedirectUrl(InvoicePayment payment, Map<String, String> adapterConfigurations) {
        String redirectUrl = payment.isSetPayerSessionInfo()
                ? payment.getPayerSessionInfo().getRedirectUrl()
                : null;
        return StringUtils.hasText(redirectUrl)
                ? redirectUrl
                : adapterConfigurations.getOrDefault(
                OptionFields.FAILED_URL.name(), adapterProperties.getFailedRedirectUrl());
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
        return sessionData != null && sessionData.isSetAuthData() && sessionData.getAuthData().isSetAuth3ds();
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

    private AdditionalTrxInfo getAdditionalTrxInfo(PaymentContext context) {
        TransactionInfo trx = context.getPaymentInfo().getPayment().getTrx();
        if (trx == null || trx.getAdditionalInfo() == null) {
            return null;
        }
        AdditionalTransactionInfo additionalTransactionInfo = trx.getAdditionalInfo();
        return AdditionalTrxInfo.builder()
                .approvalCode(additionalTransactionInfo.getApprovalCode())
                .rrn(additionalTransactionInfo.getRrn())
                .build();
    }

}

