package dev.vality.adapter.flow.lib.processor;

import dev.vality.adapter.common.processor.Processor;
import dev.vality.adapter.flow.lib.model.BaseResponseModel;
import dev.vality.adapter.flow.lib.model.GeneralEntryStateModel;
import dev.vality.adapter.flow.lib.model.GeneralExitStateModel;
import dev.vality.adapter.flow.lib.utils.ErrorUtils;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ErrorProcessor implements Processor<GeneralExitStateModel, BaseResponseModel, GeneralEntryStateModel> {

    @Override
    public GeneralExitStateModel process(BaseResponseModel response, GeneralEntryStateModel entryStateModel) {
        if (response != null && ErrorUtils.isError(response)) {
            GeneralExitStateModel exitStateModel = new GeneralExitStateModel();
            exitStateModel.setErrorCode(String.valueOf(response.getErrorCode()));
            exitStateModel.setErrorMessage(response.getErrorMessage());
            exitStateModel.setGeneralEntryStateModel(entryStateModel);
            return exitStateModel;
        }

        GeneralExitStateModel exitStateModel = new GeneralExitStateModel();
        exitStateModel.setErrorCode("unknown_error");
        exitStateModel.setErrorMessage("Unknown error reason!");
        exitStateModel.setGeneralEntryStateModel(entryStateModel);
        return exitStateModel;
    }
}