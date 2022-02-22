package dev.vality.adapter.flow.lib.flow;

import dev.vality.adapter.flow.lib.constant.Step;
import dev.vality.adapter.flow.lib.model.EntryStateModel;
import dev.vality.adapter.flow.lib.model.ExitStateModel;

public interface StepResolver<T extends EntryStateModel, R extends ExitStateModel> {

    Step resolveCurrentStep(T entryStateModel);

    Step resolveNextStep(R exitStateModel);

}