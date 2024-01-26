package dev.vality.adapter.flow.lib.processor;

import dev.vality.adapter.flow.lib.constant.MetaData;
import dev.vality.adapter.flow.lib.constant.Status;
import dev.vality.adapter.flow.lib.model.BaseResponseModel;
import dev.vality.adapter.flow.lib.model.EntryStateModel;
import dev.vality.adapter.flow.lib.model.ExitStateModel;
import dev.vality.adapter.flow.lib.utils.ErrorUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class SuccessFinishProcessor
        implements Processor<ExitStateModel, BaseResponseModel, EntryStateModel> {

    private final Processor<ExitStateModel, BaseResponseModel, EntryStateModel> nextProcessor;

    @Override
    public ExitStateModel process(BaseResponseModel response, EntryStateModel entryStateModel) {
        if (response.getStatus() == Status.SUCCESS
                && !ErrorUtils.isError(response)) {
            log.debug("Start success process response: {} entryStateModel: {}", response, entryStateModel);
            ExitStateModel exitStateModel = new ExitStateModel();
            exitStateModel.setProviderTrxId(StringUtils.hasText(response.getProviderTrxId())
                    ? response.getProviderTrxId()
                    : entryStateModel.getBaseRequestModel().getProviderTrxId());
            exitStateModel.setLastOperationStatus(response.getStatus());
            exitStateModel.setAdditionalTrxInfo(response.getAdditionalTrxInfo());
            exitStateModel.setCustomContext(response.getCustomContext());
            exitStateModel.setChangedCost(response.getChangedCost());
            Map<String, String> saveData = response.getSaveData();
            if (entryStateModel.getBaseRequestModel().getRecurrentPaymentData() != null
                    && entryStateModel.getBaseRequestModel().getRecurrentPaymentData().isMakeRecurrent()) {
                if (saveData == null) {
                    saveData = new HashMap<>();
                }
                String recToken = initRecurrentToken(response, entryStateModel);
                if (StringUtils.hasText(recToken)) {
                    saveData.put(MetaData.META_REC_TOKEN, recToken);
                    exitStateModel.setRecToken(recToken);
                }
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

    private String initRecurrentToken(BaseResponseModel response, EntryStateModel entryStateModel) {
        if (StringUtils.hasText(response.getRecurrentToken())) {
            return response.getRecurrentToken();
        } else if (entryStateModel.getBaseRequestModel().getRecurrentPaymentData() != null
                && StringUtils.hasText(entryStateModel.getBaseRequestModel().getRecurrentPaymentData()
                .getRecToken())) {
            return entryStateModel.getBaseRequestModel().getRecurrentPaymentData().getRecToken();
        }
        return null;
    }
}
