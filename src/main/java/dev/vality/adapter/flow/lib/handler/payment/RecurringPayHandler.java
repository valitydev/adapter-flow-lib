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

public class RecurringPayHandler
        extends CommonHandlerImpl<BaseRequestModel, BaseResponseModel, EntryStateModel, ExitStateModel> {

    public RecurringPayHandler(
            RemoteClient client,
            Converter<EntryStateModel, BaseRequestModel> converter,
            Processor<ExitStateModel, BaseResponseModel, EntryStateModel> responseProcessorChain
    ) {
        super(client::recurringPay, converter, responseProcessorChain);
    }

    @Override
    public boolean isHandle(EntryStateModel entryStateModel) {
        return Step.RECURRING_PAY == entryStateModel.getCurrentStep();
    }
}
