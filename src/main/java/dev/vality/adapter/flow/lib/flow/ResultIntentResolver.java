package dev.vality.adapter.flow.lib.flow;

import dev.vality.adapter.flow.lib.model.ExitStateModel;
import dev.vality.damsel.proxy_provider.Intent;

public interface ResultIntentResolver {

    Intent initIntentByStep(ExitStateModel exitStateModel);

}
