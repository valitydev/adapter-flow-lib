package dev.vality.adapter.flow.lib.flow.simple;

import dev.vality.adapter.flow.lib.constant.Status;
import dev.vality.adapter.flow.lib.constant.Step;
import dev.vality.adapter.flow.lib.flow.AbstractGenerateTokenStepResolver;
import dev.vality.adapter.flow.lib.model.EntryStateModel;
import dev.vality.adapter.flow.lib.model.ExitStateModel;

public class GenerateTokenSimpleRedirectWithPollingStepResolverImpl extends
        AbstractGenerateTokenStepResolver<EntryStateModel, ExitStateModel> {

    @Override
    public Step resolveCurrentStep(EntryStateModel stateModel) {
        Step currentStep = stateModel.getCurrentStep();
        if (currentStep != null) {
            return currentStep;
        }
        return Step.GENERATE_TOKEN;
    }

    @Override
    public Step resolveNextStep(ExitStateModel exitStateModel) {
        EntryStateModel entryStateModel = exitStateModel.getEntryStateModel();
        Step step = entryStateModel.getCurrentStep();
        return switch (step) {
            case GENERATE_TOKEN -> Step.CHECK_STATUS;
            case CHECK_STATUS -> exitStateModel.getLastOperationStatus() == Status.NEED_RETRY
                    ? Step.CHECK_STATUS
                    : Step.DO_NOTHING;
            default -> Step.DO_NOTHING;
        };
    }
}
