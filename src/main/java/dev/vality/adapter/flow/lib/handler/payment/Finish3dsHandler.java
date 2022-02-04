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

public class Finish3dsHandler
        extends CommonHandlerImpl<BaseRequestModel, BaseResponseModel, GeneralEntryStateModel, GeneralExitStateModel> {

    public Finish3dsHandler(
            RemoteClient client,
            Converter<GeneralEntryStateModel, BaseRequestModel> converter,
            Processor<GeneralExitStateModel, BaseResponseModel, GeneralEntryStateModel> responseProcessorChain
    ) {
        super(client::finish3ds, converter, responseProcessorChain);
    }

    @Override
    public boolean isHandle(GeneralEntryStateModel entryStateModel) {
        return Step.FINISH_THREE_DS_V1 == entryStateModel.getCurrentStep();
    }
}
