package dev.vality.adapter.flow.lib.handler.payment;

import dev.vality.adapter.common.handler.CommonHandlerImpl;
import dev.vality.adapter.common.processor.Processor;
import dev.vality.adapter.flow.lib.client.RemoteClient;
import dev.vality.adapter.flow.lib.constant.Step;
import dev.vality.adapter.flow.lib.model.BaseRequestModel;
import dev.vality.adapter.flow.lib.model.BaseResponseModel;
import dev.vality.adapter.flow.lib.model.GeneralEntryStateModel;
import dev.vality.adapter.flow.lib.model.GeneralExitStateModel;
import org.springframework.core.convert.converter.Converter;

public class Check3dsV2Handler
        extends CommonHandlerImpl<BaseRequestModel, BaseResponseModel, GeneralEntryStateModel, GeneralExitStateModel> {

    public Check3dsV2Handler(
            RemoteClient client,
            Converter<GeneralEntryStateModel, BaseRequestModel> converter,
            Processor<GeneralExitStateModel, BaseResponseModel, GeneralEntryStateModel> responseProcessorChain
    ) {
        super(client::check3dsV2, converter, responseProcessorChain);
    }

    @Override
    public boolean isHandle(GeneralEntryStateModel entryStateModel) {
        return Step.CHECK_NEED_3DS_V2 == entryStateModel.getCurrentStep();
    }
}
