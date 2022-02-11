package dev.vality.adapter.flow.lib.handler.payment;

import dev.vality.adapter.common.handler.CommonHandler;
import dev.vality.adapter.common.model.AdapterContext;
import dev.vality.adapter.flow.lib.constant.Step;
import dev.vality.adapter.flow.lib.model.EntryStateModel;
import dev.vality.adapter.flow.lib.model.ExitStateModel;
import org.apache.thrift.TException;


public class DoNothingHandler implements CommonHandler<ExitStateModel, EntryStateModel> {

    @Override
    public boolean isHandle(EntryStateModel model) {
        return model.getCurrentStep() == Step.DO_NOTHING;
    }

    @Override
    public ExitStateModel handle(EntryStateModel entryStateModel) throws TException {
        var exitStateModel = new ExitStateModel();
        exitStateModel.setGeneralEntryStateModel(entryStateModel);
        var adapterContext = new AdapterContext();
        adapterContext.setTrxId(entryStateModel.getBaseRequestModel().getProviderTrxId());
        exitStateModel.setNextStep(Step.DO_NOTHING);
        exitStateModel.setTrxExtra(entryStateModel.getBaseRequestModel().getSavedData());
        if (entryStateModel.getBaseRequestModel().getRecurrentPaymentData().isMakeRecurrent()) {
            exitStateModel.setRecToken(entryStateModel.getBaseRequestModel().getRecurrentPaymentData().getRecToken());
        }
        return exitStateModel;
    }

}
