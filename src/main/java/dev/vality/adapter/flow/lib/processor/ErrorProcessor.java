package dev.vality.adapter.flow.lib.processor;

import dev.vality.adapter.common.processor.Processor;
import dev.vality.adapter.flow.lib.model.BaseResponseModel;
import dev.vality.adapter.flow.lib.model.EntryStateModel;
import dev.vality.adapter.flow.lib.model.ExitStateModel;
import dev.vality.adapter.flow.lib.utils.ErrorUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ErrorProcessor implements Processor<ExitStateModel, BaseResponseModel, EntryStateModel> {

    @Override
    public ExitStateModel process(BaseResponseModel response, EntryStateModel entryStateModel) {
        log.debug("Start error process response: {} entryStateModel: {}", response, entryStateModel);

        if (response != null && ErrorUtils.isError(response)) {
            ExitStateModel exitStateModel = new ExitStateModel();
            exitStateModel.setErrorCode(String.valueOf(response.getErrorCode()));
            exitStateModel.setErrorMessage(response.getErrorMessage());
            log.debug("Finish error process response: {} entryStateModel: {}", response, entryStateModel);
            return exitStateModel;
        }

        ExitStateModel exitStateModel = new ExitStateModel();
        exitStateModel.setErrorCode("unknown_error");
        exitStateModel.setErrorMessage("Unknown error reason!");
        exitStateModel.setGeneralEntryStateModel(entryStateModel);

        log.debug("Finish error process response: {} entryStateModel: {}", response, entryStateModel);
        return exitStateModel;
    }
}