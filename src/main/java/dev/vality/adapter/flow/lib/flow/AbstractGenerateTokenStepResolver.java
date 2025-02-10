package dev.vality.adapter.flow.lib.flow;

import dev.vality.adapter.flow.lib.constant.Step;
import dev.vality.adapter.flow.lib.model.EntryStateModel;
import dev.vality.adapter.flow.lib.model.ExitStateModel;
import dev.vality.adapter.flow.lib.utils.StageFlowResolver;

public abstract class AbstractGenerateTokenStepResolver<T extends EntryStateModel, R extends ExitStateModel>
        implements StepResolver<T, R> {

    @Override
    public Step resolveCurrentStep(EntryStateModel stateModel) {
        Step currentStep = stateModel.getCurrentStep();
        if (currentStep != null) {
            return currentStep;
        }
        return StageFlowResolver.isOneStageFlow(stateModel) ? Step.PAY : Step.AUTH;
    }

}