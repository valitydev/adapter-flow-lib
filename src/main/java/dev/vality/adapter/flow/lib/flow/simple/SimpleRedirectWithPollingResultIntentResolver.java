package dev.vality.adapter.flow.lib.flow.simple;

import dev.vality.adapter.flow.lib.constant.Status;
import dev.vality.adapter.flow.lib.constant.Step;
import dev.vality.adapter.flow.lib.flow.ResultIntentResolver;
import dev.vality.adapter.flow.lib.model.EntryStateModel;
import dev.vality.adapter.flow.lib.model.ExitStateModel;
import dev.vality.adapter.flow.lib.service.IntentResultFactory;
import dev.vality.damsel.proxy_provider.Intent;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SimpleRedirectWithPollingResultIntentResolver implements ResultIntentResolver {

    private final IntentResultFactory intentResultFactory;

    @Override
    public Intent initIntentByStep(ExitStateModel exitStateModel) {
        Step nextStep = exitStateModel.getNextStep();
        EntryStateModel entryStateModel = exitStateModel.getEntryStateModel();
        Step currentStep = entryStateModel.getCurrentStep();
        return switch (nextStep) {
            case CHECK_STATUS -> exitStateModel.getLastOperationStatus() == Status.NEED_REDIRECT
                    ? intentResultFactory.createSuspendIntentWithCallbackAfterTimeout(exitStateModel)
                    : intentResultFactory.createSleepIntentWithExponentialPolling(exitStateModel);
            case DO_NOTHING -> createIntentByCurrentStep(exitStateModel, currentStep);
            case REFUND, CANCEL -> intentResultFactory.createFinishIntentSuccess();
            default -> throw new IllegalStateException("Wrong nextStep: " + nextStep);
        };
    }

    private Intent createIntentByCurrentStep(ExitStateModel exitStateModel, Step currentStep) {
        return switch (currentStep) {
            case CHECK_STATUS, CHECK_NEED_3DS_V2, FINISH_THREE_DS_V1, FINISH_THREE_DS_V2, DO_NOTHING,
                    PAY, AUTH -> intentResultFactory.createFinishIntentSuccessWithCheckToken(exitStateModel);
            case REFUND, CANCEL -> intentResultFactory.createFinishIntentSuccess();
            default -> throw new IllegalStateException("Wrong currentStep: " + currentStep);
        };
    }

}
