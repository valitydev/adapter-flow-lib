package dev.vality.adapter.flow.lib.flow.full;

import dev.vality.adapter.common.enums.TargetStatus;
import dev.vality.adapter.flow.lib.constant.*;
import dev.vality.adapter.flow.lib.flow.StepResolver;
import dev.vality.adapter.flow.lib.model.EntryStateModel;
import dev.vality.adapter.flow.lib.model.ExitStateModel;
import org.springframework.stereotype.Component;

import java.util.Objects;

public class FullThreeDsAllVersionsStepResolverImpl implements StepResolver<EntryStateModel, ExitStateModel> {

    @Override
    public Step resolveEntry(EntryStateModel stateModel) {
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
    public Step resolveExit(ExitStateModel exitStateModel) {
        EntryStateModel entryStateModel = exitStateModel.getGeneralEntryStateModel();
        Step step = entryStateModel.getCurrentStep();
        switch (step) {
            case AUTH, PAY:
                if (exitStateModel.getLastOperationStatus() == Status.NEED_REDIRECT
                        && exitStateModel.getThreeDsData() != null
                        && exitStateModel.getThreeDsData().getThreeDsType() == ThreeDsType.V1) {
                    return Step.FINISH_THREE_DS_V1;
                } else if (exitStateModel.getLastOperationStatus() == Status.NEED_REDIRECT
                        && exitStateModel.getThreeDsData() != null
                        && exitStateModel.getThreeDsData().getThreeDsType() == ThreeDsType.V2_SIMPLE) {
                    return Step.FINISH_THREE_DS_V2;
                } else if (exitStateModel.getLastOperationStatus() == Status.NEED_REDIRECT
                        && exitStateModel.getThreeDsData() != null
                        && exitStateModel.getThreeDsData().getThreeDsType() == ThreeDsType.V2_FULL) {
                    return Step.CHECK_NEED_3DS_V2;
                } else {
                    return Step.DO_NOTHING;
                }
            case CHECK_NEED_3DS_V2:
                if (exitStateModel.getLastOperationStatus() == Status.NEED_REDIRECT
                        && exitStateModel.getThreeDsData() != null) {
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