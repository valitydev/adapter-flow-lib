package dev.vality.adapter.flow.lib.flow.full;

import dev.vality.adapter.flow.lib.constant.Step;
import dev.vality.adapter.flow.lib.flow.RecurrentResultIntentResolver;
import dev.vality.adapter.flow.lib.model.ExitStateModel;
import dev.vality.adapter.flow.lib.service.factory.SimpleRecurrentIntentResultFactory;
import dev.vality.damsel.proxy_provider.RecurrentTokenIntent;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GenerateTokenResultIntentResolverImpl implements RecurrentResultIntentResolver {

    private final SimpleRecurrentIntentResultFactory recurrentIntentResultFactory;

    @Override
    public RecurrentTokenIntent initIntentByStep(ExitStateModel exitStateModel) {
        Step nextStep = exitStateModel.getNextStep();
        return switch (nextStep) {
            case REFUND, CAPTURE -> recurrentIntentResultFactory.createSleepIntentForReinvocation();
            case FINISH_THREE_DS_V1, FINISH_THREE_DS_V2 -> recurrentIntentResultFactory.createIntentWithSuspension(
                    exitStateModel);
            case DO_NOTHING -> recurrentIntentResultFactory.createFinishIntent(exitStateModel.getRecToken());
            default -> throw new IllegalStateException("Wrong state: " + nextStep);
        };
    }

}
