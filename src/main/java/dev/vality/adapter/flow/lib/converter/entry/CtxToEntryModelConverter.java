package dev.vality.adapter.flow.lib.converter.entry;

import dev.vality.adapter.common.enums.TargetStatus;
import dev.vality.adapter.common.model.AdapterContext;
import dev.vality.adapter.common.state.deserializer.AdapterDeserializer;
import dev.vality.adapter.common.state.utils.AdapterStateUtils;
import dev.vality.adapter.common.utils.converter.CardDataUtils;
import dev.vality.adapter.common.utils.converter.TargetStatusResolver;
import dev.vality.adapter.flow.lib.model.*;
import dev.vality.adapter.flow.lib.service.IdGenerator;
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
public class CtxToEntryModelConverter implements Converter<PaymentContext, GeneralEntryStateModel> {

    private final CdsClientStorage cdsStorage;
    private final AdapterDeserializer adapterDeserializer;
    private final IdGenerator idGenerator;

    @Override
    public GeneralEntryStateModel convert(PaymentContext context) {
        PaymentInfo paymentInfo = context.getPaymentInfo();
        InvoicePayment payment = paymentInfo.getPayment();
        TargetStatus targetStatus = TargetStatusResolver.extractTargetStatus(context.getSession().getTarget());
        AdapterContext adapterContext = AdapterStateUtils.getAdapterContext(context, adapterDeserializer);
        PaymentResource paymentResource = payment.getPaymentResource();

        MobilePaymentData.MobilePaymentDataBuilder mobilePaymentDataBuilder = MobilePaymentData.builder();
        dev.vality.adapter.flow.lib.model.CardData.CardDataBuilder cardDataBuilder =
                dev.vality.adapter.flow.lib.model.CardData.builder();
        if (paymentResource.isSetDisposablePaymentResource()
                && (context.getSession().getState() == null || context.getSession().getState().length == 0)
                && TargetStatus.PROCESSED == targetStatus) {
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

        String orderId = idGenerator.get(paymentInfo.getInvoice().getId()).toString();
        RecurrentPaymentData recurrentPaymentData = initRecurrentPaymentData(payment, paymentResource);
        TransactionInfo trx = payment.getTrx();
        return GeneralEntryStateModel.builder()
                .baseRequestModel(BaseRequestModel.builder().recurrentPaymentData(recurrentPaymentData)
                        .mobilePaymentData(mobilePaymentDataBuilder.build())
                        .cardData(cardDataBuilder.build())
                        .refundData(initRefundData(paymentInfo))
                        .paymentId(orderId)
                        .currency(payment.getCost().getCurrency().getSymbolicCode())
                        .amount(payment.getCost().getAmount())
                        .details(paymentInfo.getInvoice().getDetails().getDescription())
                        .payerInfo(PayerInfo.builder()
                                .ip(ProxyProviderPackageCreators.extractIpAddress(context))
                                .build())
                        .adapterConfigurations(context.getOptions())
                        .providerTrxId(trx != null ? trx.getId() : adapterContext.getTrxId())
                        .savedData(trx != null ? trx.getExtra() : new HashMap<>())
                        .build())
                .targetStatus(targetStatus)
                .redirectUrl(payment.isSetPayerSessionInfo() ? payment.getPayerSessionInfo().getRedirectUrl() : null)
                .build();
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

    private RecurrentPaymentData initRecurrentPaymentData(InvoicePayment payment, PaymentResource paymentResource) {
        RecurrentPaymentData.RecurrentPaymentDataBuilder recurrentPaymentDataBuilder = RecurrentPaymentData.builder()
                .makeRecurrent(payment.make_recurrent);
        if (paymentResource.isSetRecurrentPaymentResource()) {
            recurrentPaymentDataBuilder
                    .recToken(paymentResource.getRecurrentPaymentResource().getRecToken());
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

