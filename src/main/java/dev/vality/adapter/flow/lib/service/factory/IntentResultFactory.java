package dev.vality.adapter.flow.lib.service.factory;

import dev.vality.adapter.flow.lib.model.ExitStateModel;
import dev.vality.damsel.proxy_provider.Intent;

public interface IntentResultFactory {

    Intent createFinishIntentSuccessWithCheckToken(ExitStateModel exitStateModel);

    Intent createSuspendIntentWithFailedAfterTimeout(ExitStateModel exitStateModel);

    Intent createSuspendIntentWithCallbackAfterTimeout(ExitStateModel exitStateModel);

    Intent createFinishIntentSuccess(ExitStateModel exitStateModel);

    Intent createSleepIntentWithExponentialPolling(ExitStateModel exitStateModel);

    Intent createFinishIntentFailed(ExitStateModel exitStateModel);

    Intent createFinishIntentFailed(String errorCode, String errorMessage);
}
