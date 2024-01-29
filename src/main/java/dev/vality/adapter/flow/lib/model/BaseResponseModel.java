package dev.vality.adapter.flow.lib.model;

import dev.vality.adapter.flow.lib.constant.Status;
import dev.vality.damsel.proxy_provider.Cash;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Data
@SuperBuilder
public class BaseResponseModel {

    /**
     * Result status operation, by this status flow choose stop, retry or redirect branch.
     */
    private Status status;
    /**
     * Error code from external system or your implementation
     */
    private String errorCode;
    /**
     * Error description
     */
    private String errorMessage;
    /**
     * Uniq identifier from provider (if exists).
     */
    private String providerTrxId;
    /**
     * Token for pay in recurrent operations.
     */
    private String recurrentToken;
    /**
     * Data that you want to use in next steps.
     */
    private Map<String, String> saveData;
    /**
     * Data for choose 3ds flow and parameters for redirects.
     */
    private ThreeDsData threeDsData;
    /**
     * Data for display qr code.
     */
    private QrDisplayData qrDisplayData;
    /**
     * Data for support about transactions.
     */
    private AdditionalTrxInfo additionalTrxInfo;
    /**
     * Custom data for more flexibility
     */
    private byte[] customContext;
    /**
     * Cash that was actually received by provider.
     * Is used when payer transferred wrong amount of money.
     */
    private Cash changedCost;

}