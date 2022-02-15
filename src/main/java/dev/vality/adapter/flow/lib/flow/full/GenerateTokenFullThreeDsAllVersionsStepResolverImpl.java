package dev.vality.adapter.flow.lib.flow.full;

import dev.vality.adapter.flow.lib.constant.Status;
import dev.vality.adapter.flow.lib.constant.Step;
import dev.vality.adapter.flow.lib.constant.ThreeDsType;
import dev.vality.adapter.flow.lib.flow.AbstractGenerateTokenStepResolver;
import dev.vality.adapter.flow.lib.model.EntryStateModel;
import dev.vality.adapter.flow.lib.model.ExitStateModel;

public class GenerateTokenFullThreeDsAllVersionsStepResolverImpl extends
        AbstractGenerateTokenStepResolver<EntryStateModel, ExitStateModel> {

    @Override
    public Step resolveNextStep(ExitStateModel exitStateModel) {
        EntryStateModel entryStateModel = exitStateModel.getGeneralEntryStateModel();
        Step step = entryStateModel.getCurrentStep();
        switch (step) {
            case AUTH, PAY:
                if (exitStateModel.getLastOperationStatus() == Status.NEED_REDIRECT
                        && exitStateModel.getThreeDsData() != null
                        && exitStateModel.getThreeDsData().getThreeDsType() == ThreeDsType.V1) {
                    return Step.FINISH_THREE_DS_V1;
                } else if (exitStateModel.getLastOperationStatus() == Status.NEED_REDIRECT
                        && exitStateModel.getThreeDsData() != null
                        && exitStateModel.getThreeDsData().getThreeDsType() == ThreeDsType.V2_SIMPLE) {
                    return Step.FINISH_THREE_DS_V2;
                } else if (exitStateModel.getLastOperationStatus() == Status.NEED_REDIRECT
                        && exitStateModel.getThreeDsData() != null
                        && exitStateModel.getThreeDsData().getThreeDsType() == ThreeDsType.V2_FULL) {
                    return Step.CHECK_NEED_3DS_V2;
                } else {
                    return Step.CAPTURE;
                }
            case CHECK_NEED_3DS_V2:
                if (exitStateModel.getLastOperationStatus() == Status.NEED_REDIRECT
                        && exitStateModel.getThreeDsData() != null) {
                    return Step.FINISH_THREE_DS_V2;
                } else {
                    return Step.CAPTURE;
                }
            case FINISH_THREE_DS_V1, FINISH_THREE_DS_V2:
                return Step.CAPTURE;
            case CAPTURE:
                return Step.REFUND;
            case REFUND:
                return Step.DO_NOTHING;
            default:
                return step;
        }
    }
}
