package dev.vality.adapter.flow.lib.flow;

import dev.vality.adapter.common.enums.TargetStatus;
import dev.vality.adapter.flow.lib.constant.OptionFields;
import dev.vality.adapter.flow.lib.constant.Stage;
import dev.vality.adapter.flow.lib.constant.Step;
import dev.vality.adapter.flow.lib.constant.ThreeDsType;
import dev.vality.adapter.flow.lib.model.GeneralEntryStateModel;
import dev.vality.adapter.flow.lib.model.GeneralExitStateModel;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class StepResolverImpl implements StepResolver<GeneralEntryStateModel, GeneralExitStateModel> {

    @Override
    public Step resolveEntry(GeneralEntryStateModel stateModel) {
        TargetStatus targetStatus = stateModel.getTargetStatus();
        Step currentStep = stateModel.getCurrentStep();
        if (targetStatus == null) {
            return Objects.requireNonNullElse(currentStep, Step.AUTH);
        }
        switch (targetStatus) {
            case PROCESSED:
                if (Stage.ONE.equals(stateModel.getBaseRequestModel().getAdapterConfigurations()
                        .get(OptionFields.STAGE.name()))) {
                    return Objects.requireNonNullElse(currentStep, Step.PAY);
                } else {
                    return Objects.requireNonNullElse(currentStep, Step.AUTH);
                }
            case CAPTURED:
                if (Stage.ONE.equals(
                        stateModel.getBaseRequestModel().getAdapterConfigurations().get(OptionFields.STAGE.name()))) {
                    return Step.DO_NOTHING;
                }
                return Step.CAPTURE;
            case CANCELLED:
                return Step.CANCEL;
            case REFUNDED:
                return Step.REFUND;
            default:
                throw new IllegalStateException("Unknown state of entryState: " + targetStatus);
        }
    }

    @Override
    public Step resolveExit(GeneralExitStateModel exitStateModel) {
        GeneralEntryStateModel entryStateModel = exitStateModel.getGeneralEntryStateModel();
        Step step = entryStateModel.getCurrentStep();
        switch (step) {
            case AUTH, PAY:
                if (exitStateModel.getThreeDsData() != null
                        && exitStateModel.getThreeDsData().getThreeDsType() == ThreeDsType.V1) {
                    return Step.FINISH_THREE_DS_V1;
                } else if (exitStateModel.getThreeDsData() != null
                        && exitStateModel.getThreeDsData().getThreeDsType() == ThreeDsType.V2_SIMPLE) {
                    return Step.FINISH_THREE_DS_V2;
                } else if (exitStateModel.getThreeDsData() != null
                        && exitStateModel.getThreeDsData().getThreeDsType() == ThreeDsType.V2_FULL) {
                    return Step.CHECK_NEED_3DS_V2;
                } else {
                    return Step.DO_NOTHING;
                }
            case CHECK_NEED_3DS_V2:
                if (exitStateModel.getThreeDsData() != null) {
                    return Step.FINISH_THREE_DS_V2;
                } else {
                    return Step.DO_NOTHING;
                }
            case FINISH_THREE_DS_V1, FINISH_THREE_DS_V2, CANCEL, REFUND, CAPTURE:
                return Step.DO_NOTHING;
            default:
                return step;
        }
    }
}
