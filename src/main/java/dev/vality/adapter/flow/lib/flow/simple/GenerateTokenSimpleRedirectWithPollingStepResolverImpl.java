package dev.vality.adapter.flow.lib.flow.simple;

import dev.vality.adapter.flow.lib.constant.OptionFields;
import dev.vality.adapter.flow.lib.constant.Stage;
import dev.vality.adapter.flow.lib.constant.Status;
import dev.vality.adapter.flow.lib.constant.Step;
import dev.vality.adapter.flow.lib.flow.AbstractGenerateTokenStepResolver;
import dev.vality.adapter.flow.lib.flow.StepResolver;
import dev.vality.adapter.flow.lib.model.EntryStateModel;
import dev.vality.adapter.flow.lib.model.ExitStateModel;
import dev.vality.adapter.flow.lib.utils.StageFlowResolver;

import java.util.Objects;

public class GenerateTokenSimpleRedirectWithPollingStepResolverImpl extends
        AbstractGenerateTokenStepResolver<EntryStateModel, ExitStateModel> {

    @Override
    @SuppressWarnings(value = "indentation")
    public Step resolveNextStep(ExitStateModel exitStateModel) {
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
