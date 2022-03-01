package dev.vality.adapter.flow.lib.processor;

import dev.vality.adapter.flow.lib.constant.Status;
import dev.vality.adapter.flow.lib.model.BaseResponseModel;
import dev.vality.adapter.flow.lib.model.EntryStateModel;
import dev.vality.adapter.flow.lib.model.ExitStateModel;
import dev.vality.adapter.flow.lib.utils.ErrorUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class RetryProcessor implements Processor<ExitStateModel, BaseResponseModel, EntryStateModel> {

    private final Processor<ExitStateModel, BaseResponseModel, EntryStateModel> nextProcessor;

    @Override
    public ExitStateModel process(BaseResponseModel response, EntryStateModel entryStateModel) {
        if (response.getStatus() == Status.NEED_RETRY
                && !ErrorUtils.isError(response)) {
            log.debug("Start redirect process response: {} entryStateModel: {}", response, entryStateModel);
            ExitStateModel exitStateModel = new ExitStateModel();
            exitStateModel.setLastOperationStatus(response.getStatus());
            exitStateModel.setProviderTrxId(entryStateModel.getBaseRequestModel().getProviderTrxId());
            exitStateModel.setTrxExtra(response.getSaveData());
            log.debug("Finish redirect process response: {} entryStateModel: {}", response, entryStateModel);
            return exitStateModel;
        }

        if (nextProcessor != null) {
            return nextProcessor.process(response, entryStateModel);
        }

        throw new IllegalStateException("Processor didn't match for response " + response);
    }
}
