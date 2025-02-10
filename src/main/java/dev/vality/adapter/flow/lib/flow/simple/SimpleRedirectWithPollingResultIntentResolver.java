package dev.vality.adapter.flow.lib.flow.simple;

import dev.vality.adapter.flow.lib.constant.Status;
import dev.vality.adapter.flow.lib.constant.Step;
import dev.vality.adapter.flow.lib.constant.TargetStatus;
import dev.vality.adapter.flow.lib.flow.ResultIntentResolver;
import dev.vality.adapter.flow.lib.model.ExitStateModel;
import dev.vality.adapter.flow.lib.service.factory.IntentResultFactory;
import dev.vality.damsel.proxy_provider.Intent;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SimpleRedirectWithPollingResultIntentResolver implements ResultIntentResolver {

    private final IntentResultFactory intentResultFactory;

    @Override
    public Intent initIntentByStep(ExitStateModel exitStateModel) {
        Step nextStep = exitStateModel.getNextStep();
        return switch (nextStep) {
            case CHECK_STATUS -> exitStateModel.getLastOperationStatus() == Status.NEED_REDIRECT
                    ? intentResultFactory.createSuspendIntentWithCallbackAfterTimeout(exitStateModel)
                    : intentResultFactory.createSleepIntentWithExponentialPolling(exitStateModel);
            case DO_NOTHING -> createIntentByTargetStatus(exitStateModel);
            case REFUND, CANCEL -> intentResultFactory.createFinishIntentSuccess(exitStateModel);
            default -> throw new IllegalStateException("Wrong nextStep: " + nextStep);
        };
    }

    private Intent createIntentByTargetStatus(ExitStateModel exitStateModel) {
        if (exitStateModel.getEntryStateModel().getTargetStatus() == TargetStatus.CANCELLED
                || exitStateModel.getEntryStateModel().getTargetStatus() == TargetStatus.REFUNDED) {
            return intentResultFactory.createFinishIntentSuccess(exitStateModel);
        }
        return intentResultFactory.createFinishIntentSuccessWithCheckToken(exitStateModel);
    }

}
