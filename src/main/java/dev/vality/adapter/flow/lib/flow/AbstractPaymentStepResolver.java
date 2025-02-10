package dev.vality.adapter.flow.lib.flow;

import dev.vality.adapter.flow.lib.constant.Step;
import dev.vality.adapter.flow.lib.model.EntryStateModel;
import dev.vality.adapter.flow.lib.model.ExitStateModel;
import dev.vality.adapter.flow.lib.utils.StageFlowResolver;

public abstract class AbstractPaymentStepResolver<T extends EntryStateModel, R extends ExitStateModel>
        implements StepResolver<T, R> {

    @Override
    public Step resolveCurrentStep(T entryStateModel) {
        Step currentStep = entryStateModel.getCurrentStep();
        if (currentStep != null) {
            return currentStep;
        }
        return switch (entryStateModel.getTargetStatus()) {
            case PROCESSED -> StageFlowResolver.isOneStageFlow(entryStateModel) ? Step.PAY : Step.AUTH;
            case CAPTURED -> StageFlowResolver.isOneStageFlow(entryStateModel) ? Step.DO_NOTHING : Step.CAPTURE;
            case CANCELLED -> Step.CANCEL;
            case REFUNDED -> Step.REFUND;
        };
    }

}