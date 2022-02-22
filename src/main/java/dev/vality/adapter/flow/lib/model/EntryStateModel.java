package dev.vality.adapter.flow.lib.model;

import dev.vality.adapter.flow.lib.constant.Step;
import dev.vality.adapter.flow.lib.constant.TargetStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EntryStateModel {

    private BaseRequestModel baseRequestModel;

    private Step currentStep;
    private PollingInfo startedPollingInfo;
    private TargetStatus targetStatus;

}