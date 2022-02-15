package dev.vality.adapter.flow.lib.converter.entry;

import dev.vality.adapter.common.enums.TargetStatus;
import dev.vality.adapter.common.utils.converter.CardDataUtils;
import dev.vality.adapter.common.utils.converter.TargetStatusResolver;
import dev.vality.adapter.flow.lib.constant.MetaData;
import dev.vality.adapter.flow.lib.constant.Step;
import dev.vality.adapter.flow.lib.model.*;
import dev.vality.adapter.flow.lib.service.IdGenerator;
import dev.vality.adapter.flow.lib.service.TemporaryContextService;
import dev.vality.adapter.flow.lib.serde.TemporaryContextDeserializer;
import dev.vality.cds.client.storage.CdsClientStorage;
import dev.vality.cds.client.storage.utils.BankCardExtractor;
import dev.vality.cds.storage.Auth3DS;
import dev.vality.cds.storage.CardData;
import dev.vality.cds.storage.SessionData;
import dev.vality.damsel.domain.BankCard;
import dev.vality.damsel.domain.TransactionInfo;
import dev.vality.damsel.proxy_provider.*;
import dev.vality.java.cds.utils.model.CardDataProxyModel;
import dev.vality.java.damsel.utils.creators.ProxyProviderPackageCreators;
import dev.vality.java.damsel.utils.extractors.ProxyProviderPackageExtractors;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@RequiredArgsConstructor
public class CtxToEntryModelConverter implements Converter<PaymentContext, EntryStateModel> {

    private final CdsClientStorage cdsStorage;
    private final TemporaryContextDeserializer temporaryContextDeserializer;
    private final IdGenerator idGenerator;
    private final TemporaryContextService temporaryContextService;

    @Override
    public EntryStateModel convert(PaymentContext context) {
        var paymentInfo = context.getPaymentInfo();
        var payment = paymentInfo.getPayment();
        var temporaryContext = temporaryContextService.getTemporaryContext(context, temporaryContextDeserializer);
        var paymentResource = payment.getPaymentResource();
        var mobilePaymentDataBuilder = MobilePaymentData.builder();
        var cardDataBuilder = dev.vality.adapter.flow.lib.model.CardData.builder();

        Step currentStep = temporaryContext.getNextStep();
        TargetStatus targetStatus = TargetStatusResolver.extractTargetStatus(context.getSession().getTarget());
        initPaymentData(context, paymentResource, mobilePaymentDataBuilder, cardDataBuilder, currentStep, targetStatus);

        TransactionInfo trx = payment.getTrx();
        RecurrentPaymentData recurrentPaymentData = initRecurrentPaymentData(payment, paymentResource, trx);
        return EntryStateModel.builder()
                .baseRequestModel(BaseRequestModel.builder().recurrentPaymentData(recurrentPaymentData)
                        .mobilePaymentData(mobilePaymentDataBuilder.build())
                        .cardData(cardDataBuilder.build())
                        .refundData(initRefundData(paymentInfo))
                        .paymentId(idGenerator.get(paymentInfo.getInvoice().getId()).toString())
                        .currency(payment.getCost().getCurrency().getSymbolicCode())
                        .amount(payment.getCost().getAmount())
                        .details(paymentInfo.getInvoice().getDetails().getDescription())
                        .payerInfo(PayerInfo.builder()
                                .ip(ProxyProviderPackageCreators.extractIpAddress(context))
                                .build())
                        .adapterConfigurations(context.getOptions())
                        .providerTrxId(trx != null ? trx.getId() : temporaryContext.getProviderTrxId())
                        .savedData(trx != null ? trx.getExtra() : new HashMap<>())
                        .build())
                .targetStatus(targetStatus)
                .currentStep(currentStep)
                .redirectUrl(payment.isSetPayerSessionInfo() ? payment.getPayerSessionInfo().getRedirectUrl() : null)
                .build();
    }

    private void initPaymentData(PaymentContext context, PaymentResource paymentResource,
                                 MobilePaymentData.MobilePaymentDataBuilder<?, ?> mobilePaymentDataBuilder,
                                 dev.vality.adapter.flow.lib.model.CardData.CardDataBuilder<?, ?> cardDataBuilder,
                                 Step currentStep, TargetStatus targetStatus) {
        if (paymentResource.isSetDisposablePaymentResource()
                && currentStep == null
                && targetStatus == TargetStatus.PROCESSED) {
            SessionData sessionData = cdsStorage.getSessionData(context);
            if (sessionData.isSetAuthData() && sessionData.getAuthData().isSetAuth3ds()) {
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
            CardData cardData = cdsStorage.getCardData(cardToken);
            BankCard bankCard = ProxyProviderPackageExtractors.extractBankCard(context);
            return BankCardExtractor.initCardDataProxyModel(bankCard, cardData);
        }
        return cdsStorage.getCardData(context);
    }
}

