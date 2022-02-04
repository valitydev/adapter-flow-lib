package dev.vality.adapter.flow.lib.model;

import dev.vality.adapter.flow.lib.constant.Step;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class GeneralExitStateModel {

    private String errorCode;
    private String errorMessage;
    private Step nextStep;
    private GeneralEntryStateModel generalEntryStateModel;

    private String providerTrxId;
    private Map<String, String> trxExtra;
    private ThreeDsData threeDsData;

    private String recToken;

}