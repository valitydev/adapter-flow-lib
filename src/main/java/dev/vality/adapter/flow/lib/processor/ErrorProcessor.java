package dev.vality.adapter.flow.lib.processor;

import dev.vality.adapter.flow.lib.model.BaseResponseModel;
import dev.vality.adapter.flow.lib.model.EntryStateModel;
import dev.vality.adapter.flow.lib.model.ExitStateModel;
import dev.vality.adapter.flow.lib.utils.ErrorUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

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
            exitStateModel.setAdditionalTrxInfo(response.getAdditionalTrxInfo());
            exitStateModel.setCustomContext(response.getCustomContext());
            exitStateModel.setProviderTrxId(
                    StringUtils.hasText(response.getProviderTrxId())
                            ? response.getProviderTrxId()
                            : entryStateModel.getBaseRequestModel().getProviderTrxId());
            log.debug("Finish error process response: {} entryStateModel: {}", response, entryStateModel);
            return exitStateModel;
        }

        ExitStateModel exitStateModel = new ExitStateModel();
        exitStateModel.setErrorCode("unknown_error");
        exitStateModel.setErrorMessage("Unknown error reason!");
        exitStateModel.setEntryStateModel(entryStateModel);

        log.debug("Finish error process response: {} entryStateModel: {}", response, entryStateModel);
        return exitStateModel;
    }
}