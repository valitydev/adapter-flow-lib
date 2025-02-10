package dev.vality.adapter.flow.lib.handler.payment;

import dev.vality.adapter.flow.lib.constant.Step;
import dev.vality.adapter.flow.lib.handler.CommonHandler;
import dev.vality.adapter.flow.lib.model.EntryStateModel;
import dev.vality.adapter.flow.lib.model.ExitStateModel;
import org.apache.thrift.TException;


public class DoNothingHandler implements CommonHandler<ExitStateModel, EntryStateModel> {

    @Override
    public ExitStateModel handle(EntryStateModel entryStateModel) throws TException {
        var exitStateModel = new ExitStateModel();
        exitStateModel.setEntryStateModel(entryStateModel);
        exitStateModel.setProviderTrxId(entryStateModel.getBaseRequestModel().getProviderTrxId());
        exitStateModel.setNextStep(Step.DO_NOTHING);
        exitStateModel.setTrxExtra(entryStateModel.getBaseRequestModel().getSavedData());
        exitStateModel.setAdditionalTrxInfo(entryStateModel.getBaseRequestModel().getAdditionalTrxInfo());
        if (entryStateModel.getBaseRequestModel().getRecurrentPaymentData().isMakeRecurrent()) {
            exitStateModel.setRecToken(entryStateModel.getBaseRequestModel().getRecurrentPaymentData().getRecToken());
        }
        return exitStateModel;
    }

    @Override
    public boolean isHandle(EntryStateModel model) {
        return model.getCurrentStep() == Step.DO_NOTHING;
    }

}
