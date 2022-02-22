package dev.vality.adapter.flow.lib.flow.full;

import dev.vality.adapter.flow.lib.constant.Step;
import dev.vality.adapter.flow.lib.flow.AbstractGenerateTokenStepResolver;
import dev.vality.adapter.flow.lib.model.EntryStateModel;
import dev.vality.adapter.flow.lib.model.ExitStateModel;

public class GenerateTokenFullThreeDsAllVersionsStepResolverImpl extends
        AbstractGenerateTokenStepResolver<EntryStateModel, ExitStateModel> {

    @Override
    public Step resolveNextStep(ExitStateModel exitStateModel) {
        EntryStateModel entryStateModel = exitStateModel.getEntryStateModel();
        Step step = entryStateModel.getCurrentStep();
        switch (step) {
            case AUTH, PAY:
                if (ThreeDsBranchResolver.isRedirectForThreeDsV1(exitStateModel)) {
                    return Step.FINISH_THREE_DS_V1;
                } else if (ThreeDsBranchResolver.isRedirectForThreeDsV2Simple(exitStateModel)) {
                    return Step.FINISH_THREE_DS_V2;
                } else if (ThreeDsBranchResolver.isRedirectForThreeDsV2Full(exitStateModel)) {
                    return Step.CHECK_NEED_3DS_V2;
                } else {
                    return Step.CAPTURE;
                }
            case CHECK_NEED_3DS_V2:
                if (ThreeDsBranchResolver.isRedirectForThreeDsV2Full(exitStateModel)) {
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
