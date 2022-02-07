package dev.vality.adapter.flow.lib.processor;

import dev.vality.adapter.common.model.AdapterContext;
import dev.vality.adapter.common.processor.Processor;
import dev.vality.adapter.flow.lib.constant.MetaData;
import dev.vality.adapter.flow.lib.model.BaseResponseModel;
import dev.vality.adapter.flow.lib.model.GeneralEntryStateModel;
import dev.vality.adapter.flow.lib.model.GeneralExitStateModel;
import dev.vality.adapter.flow.lib.utils.ErrorUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class SuccessFinishProcessor
        implements Processor<GeneralExitStateModel, BaseResponseModel, GeneralEntryStateModel> {

    private final Processor<GeneralExitStateModel, BaseResponseModel, GeneralEntryStateModel> nextProcessor;

    @Override
    public GeneralExitStateModel process(BaseResponseModel response, GeneralEntryStateModel entryStateModel) {
        if (!ErrorUtils.isError(response)) {
            log.debug("Start success process response: {} entryStateModel: {}", response, entryStateModel);
            AdapterContext adapterContext = new AdapterContext();
            adapterContext.setTrxId(response.getProviderTrxId());
            GeneralExitStateModel exitStateModel = new GeneralExitStateModel();
            exitStateModel.setGeneralEntryStateModel(entryStateModel);
            exitStateModel.setProviderTrxId(response.getProviderTrxId());
            Map<String, String> saveData = response.getSaveData();
            if (entryStateModel.getBaseRequestModel().getRecurrentPaymentData() != null
                    && entryStateModel.getBaseRequestModel().getRecurrentPaymentData().isMakeRecurrent()) {
                if (saveData == null) {
                    saveData = new HashMap<>();
                }
                saveData.put(MetaData.META_REC_TOKEN, response.getRecurrentToken());
                exitStateModel.setRecToken(response.getRecurrentToken());
            }
            exitStateModel.setTrxExtra(saveData);
            log.debug("Finish success process response: {} entryStateModel: {}", response, entryStateModel);
            return exitStateModel;
        }

        if (nextProcessor != null) {
            return nextProcessor.process(response, entryStateModel);
        }

        throw new IllegalStateException("Processor didn't match for response " + response);
    }
}
