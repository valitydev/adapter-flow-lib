package dev.vality.adapter.flow.lib.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BaseRequestModel {

    private String paymentId;
    private String providerTrxId;

    private Long amount;
    private String currency;
    private String createdAt;
    private String details;

    private CardData cardData;
    private PayerInfo payerInfo;
    private MobilePaymentData mobilePaymentData;
    private RecurrentPaymentData recurrentPaymentData;
    private RefundData refundData;

    //Options
    private Map<String, String> adapterConfigurations;
    private Map<String, String> savedData;
    // All fields from callback mpi page
    private Map<String, String> threeDsData;

}
