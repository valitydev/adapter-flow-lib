package dev.vality.adapter.flow.lib.flow.simple;

import dev.vality.adapter.flow.lib.constant.Status;
import dev.vality.adapter.flow.lib.constant.Step;
import dev.vality.adapter.flow.lib.flow.AbstractPaymentStepResolver;
import dev.vality.adapter.flow.lib.model.EntryStateModel;
import dev.vality.adapter.flow.lib.model.ExitStateModel;

public class SimpleRedirectWithPollingStepResolverImpl
        extends AbstractPaymentStepResolver<EntryStateModel, ExitStateModel> {

    @Override
    public Step resolveNextStep(ExitStateModel exitStateModel) {
        EntryStateModel entryStateModel = exitStateModel.getEntryStateModel();
        Step step = entryStateModel.getCurrentStep();
        return switch (step) {
            case AUTH, PAY, CAPTURE, REFUND, CANCEL -> Step.CHECK_STATUS;
            case CHECK_STATUS -> exitStateModel.getLastOperationStatus() == Status.NEED_RETRY
                    ? Step.CHECK_STATUS
                    : Step.DO_NOTHING;
            default -> step;
        };
    }
}
