package dev.vality.adapter.flow.lib.handler.payment;

import dev.vality.adapter.flow.lib.client.RemoteClient;
import dev.vality.adapter.flow.lib.constant.Step;
import dev.vality.adapter.flow.lib.handler.CommonHandlerImpl;
import dev.vality.adapter.flow.lib.model.BaseRequestModel;
import dev.vality.adapter.flow.lib.model.BaseResponseModel;
import dev.vality.adapter.flow.lib.model.EntryStateModel;
import dev.vality.adapter.flow.lib.model.ExitStateModel;
import dev.vality.adapter.flow.lib.processor.Processor;
import org.springframework.core.convert.converter.Converter;

public class Finish3dsV2Handler
        extends CommonHandlerImpl<BaseRequestModel, BaseResponseModel, EntryStateModel, ExitStateModel> {

    public Finish3dsV2Handler(
            RemoteClient client,
            Converter<EntryStateModel, BaseRequestModel> converter,
            Processor<ExitStateModel, BaseResponseModel, EntryStateModel> responseProcessorChain
    ) {
        super(client::finish3dsV2, converter, responseProcessorChain);
    }

    @Override
    public boolean isHandle(EntryStateModel entryStateModel) {
        return Step.FINISH_THREE_DS_V2 == entryStateModel.getCurrentStep();
    }
}
