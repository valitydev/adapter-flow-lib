package dev.vality.adapter.flow.lib.flow.full;

import dev.vality.adapter.flow.lib.constant.Step;
import dev.vality.adapter.flow.lib.flow.ResultIntentResolver;
import dev.vality.adapter.flow.lib.model.EntryStateModel;
import dev.vality.adapter.flow.lib.model.ExitStateModel;
import dev.vality.adapter.flow.lib.service.factory.IntentResultFactory;
import dev.vality.damsel.proxy_provider.Intent;
import lombok.RequiredArgsConstructor;

import static dev.vality.adapter.common.damsel.ProxyProviderPackageCreators.createFinishIntentSuccess;

@RequiredArgsConstructor
public class ResultIntentResolverImpl implements ResultIntentResolver {

    private final IntentResultFactory intentResultFactory;

    @Override
    public Intent initIntentByStep(ExitStateModel exitStateModel) {
        Step nextStep = exitStateModel.getNextStep();
        EntryStateModel entryStateModel = exitStateModel.getEntryStateModel();
        Step currentStep = entryStateModel.getCurrentStep();
        return switch (nextStep) {
            case FINISH_THREE_DS_V1, CHECK_NEED_3DS_V2, FINISH_THREE_DS_V2 -> intentResultFactory
                    .createSuspendIntentWithFailedAfterTimeout(exitStateModel);
            case DO_NOTHING -> createIntentByCurrentStep(exitStateModel, currentStep);
            case CAPTURE, REFUND, CANCEL -> createFinishIntentSuccess();
            default -> throw new IllegalStateException("Wrong nextStep: " + nextStep);
        };
    }

    private Intent createIntentByCurrentStep(ExitStateModel exitStateModel, Step currentStep) {
        return switch (currentStep) {
            case CHECK_NEED_3DS_V2, FINISH_THREE_DS_V1, FINISH_THREE_DS_V2,
                    DO_NOTHING, PAY, AUTH, CAPTURE -> intentResultFactory
                    .createFinishIntentSuccessWithCheckToken(exitStateModel);
            case REFUND, CANCEL -> createFinishIntentSuccess();
            default -> throw new IllegalStateException("Wrong currentStep: " + currentStep);
        };
    }

}
