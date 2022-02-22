package dev.vality.adapter.flow.lib.flow;

import dev.vality.adapter.flow.lib.model.ExitStateModel;
import dev.vality.damsel.proxy_provider.RecurrentTokenIntent;

public interface RecurrentResultIntentResolver {

    RecurrentTokenIntent initIntentByStep(ExitStateModel exitStateModel);

}
