package dev.vality.adapter.flow.lib.flow.full;

import dev.vality.adapter.flow.lib.constant.Step;
import dev.vality.adapter.flow.lib.flow.AbstractPaymentStepResolver;
import dev.vality.adapter.flow.lib.model.EntryStateModel;
import dev.vality.adapter.flow.lib.model.ExitStateModel;

public class FullThreeDsAllVersionsStepResolverImpl
        extends AbstractPaymentStepResolver<EntryStateModel, ExitStateModel> {

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
                    return Step.DO_NOTHING;
                }
            case CHECK_NEED_3DS_V2:
                if (ThreeDsBranchResolver.isRedirectForThreeDsV2Full(exitStateModel)) {
                    return Step.FINISH_THREE_DS_V2;
                } else {
                    return Step.DO_NOTHING;
                }
            case FINISH_THREE_DS_V1, FINISH_THREE_DS_V2, CANCEL, REFUND, CAPTURE:
                return Step.DO_NOTHING;
            default:
                return step;
        }
    }

}
