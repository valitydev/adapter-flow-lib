package dev.vality.adapter.flow.lib.service.factory;

import dev.vality.adapter.flow.lib.model.ExitStateModel;
import dev.vality.damsel.proxy_provider.RecurrentTokenIntent;

public interface RecurrentIntentResultFactory {

    RecurrentTokenIntent createIntentWithSuspension(ExitStateModel exitStateModel);

    RecurrentTokenIntent createSleepIntentForReinvocation();

    RecurrentTokenIntent createSleepIntentWithExponentialPolling(ExitStateModel exitStateModel);

    RecurrentTokenIntent createFinishIntent(String recToken);

    RecurrentTokenIntent createFinishIntentFailed(String errorCode, String errorMessage);
}
