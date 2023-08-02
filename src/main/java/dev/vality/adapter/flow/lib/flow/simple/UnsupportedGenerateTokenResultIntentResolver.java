package dev.vality.adapter.flow.lib.flow.simple;

import dev.vality.adapter.flow.lib.flow.RecurrentResultIntentResolver;
import dev.vality.adapter.flow.lib.model.ExitStateModel;
import dev.vality.adapter.flow.lib.service.factory.SimpleRecurrentIntentResultFactory;
import dev.vality.damsel.proxy_provider.RecurrentTokenIntent;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UnsupportedGenerateTokenResultIntentResolver implements RecurrentResultIntentResolver {

    @Override
    public RecurrentTokenIntent initIntentByStep(ExitStateModel exitStateModel) {
        throw new UnsupportedOperationException();
    }

}
