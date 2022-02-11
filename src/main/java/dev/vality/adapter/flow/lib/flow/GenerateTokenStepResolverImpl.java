package dev.vality.adapter.flow.lib.flow;

import dev.vality.adapter.flow.lib.constant.OptionFields;
import dev.vality.adapter.flow.lib.constant.Stage;
import dev.vality.adapter.flow.lib.constant.Step;
import dev.vality.adapter.flow.lib.constant.ThreeDsType;
import dev.vality.adapter.flow.lib.model.EntryStateModel;
import dev.vality.adapter.flow.lib.model.ExitStateModel;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class GenerateTokenStepResolverImpl implements StepResolver<EntryStateModel, ExitStateModel> {

    @Override
    public Step resolveEntry(EntryStateModel stateModel) {
        Step currentStep = stateModel.getCurrentStep();
        if (Stage.ONE.equals(stateModel.getBaseRequestModel().getAdapterConfigurations()
                .get(OptionFields.STAGE.name()))) {
            return Objects.requireNonNullElse(currentStep, Step.PAY);
        } else {
            return Objects.requireNonNullElse(currentStep, Step.AUTH);
        }
    }

    @Override
    public Step resolveExit(ExitStateModel exitStateModel) {
        EntryStateModel entryStateModel = exitStateModel.getGeneralEntryStateModel();
        Step step = entryStateModel.getCurrentStep();
        switch (step) {
            case AUTH, PAY:
                if (exitStateModel.getThreeDsData() != null
                        && exitStateModel.getThreeDsData().getThreeDsType() == ThreeDsType.V1) {
                    return Step.FINISH_THREE_DS_V1;
                } else if (exitStateModel.getThreeDsData() != null
                        && exitStateModel.getThreeDsData().getThreeDsType() == ThreeDsType.V2_SIMPLE) {
                    return Step.FINISH_THREE_DS_V2;
                } else if (exitStateModel.getThreeDsData() != null
                        && exitStateModel.getThreeDsData().getThreeDsType() == ThreeDsType.V2_FULL) {
                    return Step.CHECK_NEED_3DS_V2;
                } else {
                    return Step.CAPTURE;
                }
            case CHECK_NEED_3DS_V2:
                if (exitStateModel.getThreeDsData() != null) {
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
