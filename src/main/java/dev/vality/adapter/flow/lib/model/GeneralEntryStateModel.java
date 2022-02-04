package dev.vality.adapter.flow.lib.model;

import dev.vality.adapter.common.enums.TargetStatus;
import dev.vality.adapter.common.model.PollingInfo;
import dev.vality.adapter.flow.lib.constant.Step;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class GeneralEntryStateModel {

    private BaseRequestModel baseRequestModel;

    private Step currentStep;
    private PollingInfo pollingInfo;
    private TargetStatus targetStatus;
    private String redirectUrl;

}