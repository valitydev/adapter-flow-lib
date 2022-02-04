package dev.vality.adapter.flow.lib.processor;

import dev.vality.adapter.common.processor.Processor;
import dev.vality.adapter.flow.lib.model.BaseResponseModel;
import dev.vality.adapter.flow.lib.model.GeneralEntryStateModel;
import dev.vality.adapter.flow.lib.model.GeneralExitStateModel;
import dev.vality.adapter.flow.lib.utils.ErrorUtils;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RedirectProcessor implements Processor<GeneralExitStateModel, BaseResponseModel, GeneralEntryStateModel> {

    private final Processor<GeneralExitStateModel, BaseResponseModel, GeneralEntryStateModel> nextProcessor;

    @Override
    public GeneralExitStateModel process(BaseResponseModel response, GeneralEntryStateModel entryStateModel) {
        if (!ErrorUtils.isError(response)
                && response.getThreeDsData() != null
                && response.getThreeDsData().getThreeDsType() != null) {
            GeneralExitStateModel exitStateModel = new GeneralExitStateModel();
            exitStateModel.setThreeDsData(response.getThreeDsData());
            exitStateModel.setGeneralEntryStateModel(entryStateModel);
            return exitStateModel;
        }

        if (nextProcessor != null) {
            return nextProcessor.process(response, entryStateModel);
        }

        throw new IllegalStateException("Processor didn't match for response " + response);
    }
}
