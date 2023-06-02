package dev.vality.adapter.flow.lib.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * Base class with all needed fields for pay and other operations
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BaseRequestModel {

    /**
     * Uniq Long identifier for payment.
     */
    private Long paymentId;
    /**
     * Uniq String identifier for payment invoice format.
     */
    private String invoiceFormatPaymentId;
    /**
     * Uniq identifier from provider (if exists), not come in first methods.
     */
    private String providerTrxId;
    /**
     * Amount in Long format and in minimum payment units (example: cents).
     */
    private Long amount;
    /**
     * Code and symbolic code of currency.
     */
    private Currency currency;
    /**
     * Timestamp RFC 3339.
     * <p>
     * The string must contain the date and time in UTC in the following format:
     * `2016-03-22T06:12:27Z`
     */
    private String createdAt;
    /**
     * Description payment from merchant.
     */
    private String details;
    /**
     * Card data, can be empty on some steps and recurrent or mobile operations.
     */
    private CardData cardData;
    /**
     * Info about payer, send from merchant and can be empty.
     */
    private PayerInfo payerInfo;
    /**
     * Data for mobile pay methods.
     */
    private MobilePaymentData mobilePaymentData;
    /**
     * Data for recurrent payments.
     */
    private RecurrentPaymentData recurrentPaymentData;
    /**
     * Full needed for refund data.
     */
    private RefundData refundData;
    /**
     * The URL to which to redirect if the 3ds stream completed successfully.
     */
    private String successRedirectUrl;
    /**
     * The URL to which to redirect if the 3ds stream completed fails.
     */
    private String failedRedirectUrl;
    /**
     * All static parameters from configuration by support team.
     * (options - in old naming)
     */
    @ToString.Exclude
    private Map<String, String> adapterConfigurations;
    /**
     * Data that you send to save on previous steps.
     */
    private Map<String, String> savedData;
    /**
     * Additional trx info from previous steps.
     */
    private AdditionalTrxInfo additionalTrxInfo;
    /**
     * Data that comes in a callback from mpi after the 3ds step.
     * Parameter names can be unique to your implementation, only you can know.
     */
    private Map<String, String> threeDsDataFromMpiCallback;
    /**
     * Custom data for more flexibility
     */
    private byte[] customContext;

}
