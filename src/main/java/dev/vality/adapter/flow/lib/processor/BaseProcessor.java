package dev.vality.adapter.flow.lib.processor;

import dev.vality.adapter.common.model.AdapterContext;
import dev.vality.adapter.common.processor.Processor;
import dev.vality.adapter.flow.lib.model.BaseResponseModel;
import dev.vality.adapter.flow.lib.model.GeneralEntryStateModel;
import dev.vality.adapter.flow.lib.model.GeneralExitStateModel;
import dev.vality.adapter.flow.lib.utils.ErrorUtils;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BaseProcessor implements Processor<GeneralExitStateModel, BaseResponseModel, GeneralEntryStateModel> {

    private final Processor<GeneralExitStateModel, BaseResponseModel, GeneralEntryStateModel> nextProcessor;

    @Override
    public GeneralExitStateModel process(BaseResponseModel response, GeneralEntryStateModel entryStateModel) {
        if (!ErrorUtils.isError(response)) {
            AdapterContext adapterContext = new AdapterContext();
            adapterContext.setTrxId(response.getProviderTrxId());
            GeneralExitStateModel exitStateModel = new GeneralExitStateModel();
            exitStateModel.setTrxExtra(response.getSaveData());
            exitStateModel.setGeneralEntryStateModel(entryStateModel);
            exitStateModel.setProviderTrxId(response.getProviderTrxId());
            if (entryStateModel.getBaseRequestModel().getRecurrentPaymentData() != null
                    && entryStateModel.getBaseRequestModel().getRecurrentPaymentData().isMakeRecurrent()) {
                exitStateModel.setRecToken(response.getRecurrentToken());
            }
            return exitStateModel;
        }

        if (nextProcessor != null) {
            return nextProcessor.process(response, entryStateModel);
        }

        throw new IllegalStateException("Processor didn't match for response " + response);
    }
}
