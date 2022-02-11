package dev.vality.adapter.flow.lib.flow.simple;

import dev.vality.adapter.flow.lib.constant.*;
import dev.vality.adapter.flow.lib.flow.StepResolver;
import dev.vality.adapter.flow.lib.model.EntryStateModel;
import dev.vality.adapter.flow.lib.model.ExitStateModel;

import java.util.Objects;

public class GenerateTokenSimpleRedirectWithPollingStepResolverImpl
        implements StepResolver<EntryStateModel, ExitStateModel> {

    @Override
    public Step resolveEntry(EntryStateModel stateModel) {
        Step currentStep = stateModel.getCurrentStep();
        if (Stage.ONE.equals(stateModel.getBaseRequestModel().getAdapterConfigurations()
                .get(OptionFields.STAGE.name()))) {
            return Objects.requireNonNullElse(currentStep, Step.PAY);
        } else {
            return Objects.requireNonNullElse(currentStep, Step.AUTH);
        }
    }

    @Override
    public Step resolveExit(ExitStateModel exitStateModel) {
        EntryStateModel entryStateModel = exitStateModel.getGeneralEntryStateModel();
        Step step = entryStateModel.getCurrentStep();
        switch (step) {
            case AUTH, PAY:
                    return Step.CHECK_STATUS;
            case CHECK_STATUS:
                if (exitStateModel.getLastOperationStatus() == Status.NEED_RETRY) {
                    return Step.CHECK_STATUS;
                } else {
                    return Step.CAPTURE;
                }
            case CAPTURE:
                return Step.REFUND;
            case REFUND:
                return Step.DO_NOTHING;
            default:
                return step;
        }
    }
}
