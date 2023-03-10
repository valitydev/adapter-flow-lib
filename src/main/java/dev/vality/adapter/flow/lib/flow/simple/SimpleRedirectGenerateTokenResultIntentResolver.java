package dev.vality.adapter.flow.lib.flow.simple;

import dev.vality.adapter.flow.lib.constant.Step;
import dev.vality.adapter.flow.lib.flow.RecurrentResultIntentResolver;
import dev.vality.adapter.flow.lib.model.ExitStateModel;
import dev.vality.adapter.flow.lib.service.factory.SimpleRecurrentIntentResultFactory;
import dev.vality.damsel.proxy_provider.RecurrentTokenIntent;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SimpleRedirectGenerateTokenResultIntentResolver implements RecurrentResultIntentResolver {

    private final SimpleRecurrentIntentResultFactory recurrentIntentResultFactory;

    @Override
    public RecurrentTokenIntent initIntentByStep(ExitStateModel exitStateModel) {
        Step nextStep = exitStateModel.getNextStep();
        Step currentStep = exitStateModel.getEntryStateModel().getCurrentStep();
        return switch (nextStep) {
            case CHECK_STATUS -> currentStep == Step.GENERATE_TOKEN
                    ? recurrentIntentResultFactory.createIntentWithSuspension(exitStateModel)
                    : recurrentIntentResultFactory.createSleepIntentWithExponentialPolling(exitStateModel);
            case DO_NOTHING -> recurrentIntentResultFactory.createFinishIntent(exitStateModel.getRecToken());
            default -> throw new IllegalStateException("Wrong state: " + nextStep);
        };
    }

}
