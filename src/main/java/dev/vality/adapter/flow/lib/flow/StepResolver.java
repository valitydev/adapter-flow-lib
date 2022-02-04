package dev.vality.adapter.flow.lib.flow;

import dev.vality.adapter.flow.lib.constant.Step;
import dev.vality.adapter.flow.lib.model.GeneralEntryStateModel;
import dev.vality.adapter.flow.lib.model.GeneralExitStateModel;

public interface StepResolver<T extends GeneralEntryStateModel, R extends GeneralExitStateModel> {

    Step resolveEntry(T entryStateModel);

    Step resolveExit(R exitStateModel);

}