package dev.vality.adapter.flow.lib.flow.simple;

import dev.vality.adapter.common.enums.TargetStatus;
import dev.vality.adapter.flow.lib.constant.OptionFields;
import dev.vality.adapter.flow.lib.constant.Stage;
import dev.vality.adapter.flow.lib.constant.Status;
import dev.vality.adapter.flow.lib.constant.Step;
import dev.vality.adapter.flow.lib.flow.StepResolver;
import dev.vality.adapter.flow.lib.model.EntryStateModel;
import dev.vality.adapter.flow.lib.model.ExitStateModel;

import java.util.Objects;

public class SimpleRedirectWIthPollingStepResolverImpl implements StepResolver<EntryStateModel, ExitStateModel> {

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
                return Objects.requireNonNullElse(currentStep, Step.CAPTURE);
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
            case AUTH, PAY, CAPTURE:
                return Step.CHECK_STATUS;
            case CHECK_STATUS:
                if (exitStateModel.getLastOperationStatus() == Status.NEED_RETRY) {
                    return Step.CHECK_STATUS;
                } else {
                    return Step.DO_NOTHING;
                }
            case CANCEL, REFUND:
                return Step.DO_NOTHING;
            default:
                return step;
        }
    }
}
