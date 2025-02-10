package dev.vality.adapter.flow.lib.flow.simple;

import dev.vality.adapter.flow.lib.constant.Step;
import dev.vality.adapter.flow.lib.flow.AbstractGenerateTokenStepResolver;
import dev.vality.adapter.flow.lib.model.EntryStateModel;
import dev.vality.adapter.flow.lib.model.ExitStateModel;

public class UnsupportedGenerateTokenStepResolverImpl extends
        AbstractGenerateTokenStepResolver<EntryStateModel, ExitStateModel> {

    @Override
    public Step resolveCurrentStep(EntryStateModel stateModel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Step resolveNextStep(ExitStateModel exitStateModel) {
        throw new UnsupportedOperationException();
    }
}
