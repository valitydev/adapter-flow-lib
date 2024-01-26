package dev.vality.adapter.flow.lib.model;

import dev.vality.adapter.flow.lib.constant.Status;
import dev.vality.adapter.flow.lib.constant.Step;
import dev.vality.damsel.proxy_provider.Cash;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ExitStateModel {

    private Status lastOperationStatus;

    private String errorCode;
    private String errorMessage;
    private Step nextStep;
    private EntryStateModel entryStateModel;

    private String providerTrxId;
    private Map<String, String> trxExtra;
    private PollingInfo pollingInfo;
    private ThreeDsData threeDsData;
    private QrDisplayData qrDisplayData;
    private AdditionalTrxInfo additionalTrxInfo;

    private String recToken;
    private byte[] customContext;
    private Cash changedCost;

}