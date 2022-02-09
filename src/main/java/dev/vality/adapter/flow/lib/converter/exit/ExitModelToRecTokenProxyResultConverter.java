package dev.vality.adapter.flow.lib.converter.exit;

import dev.vality.adapter.flow.lib.model.GeneralExitStateModel;
import dev.vality.adapter.flow.lib.service.RecurrentResultIntentResolver;
import dev.vality.adapter.flow.lib.utils.AdapterSerializer;
import dev.vality.damsel.proxy_provider.RecurrentTokenIntent;
import dev.vality.damsel.proxy_provider.RecurrentTokenProxyResult;
import dev.vality.error.mapping.ErrorMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import static dev.vality.java.damsel.utils.creators.DomainPackageCreators.createTransactionInfo;
import static dev.vality.java.damsel.utils.creators.ProxyProviderPackageCreators.createRecurrentTokenProxyResultFailure;

@Component
@RequiredArgsConstructor
public class ExitModelToRecTokenProxyResultConverter
        implements Converter<GeneralExitStateModel, RecurrentTokenProxyResult> {

    private final ErrorMapping errorMapping;
    private final AdapterSerializer serializer;
    private final RecurrentResultIntentResolver recurrentResultIntentResolver;

    @Override
    public RecurrentTokenProxyResult convert(GeneralExitStateModel exitStateModel) {
        //---error---
        if (exitStateModel.getErrorCode() != null) {
            return createRecurrentTokenProxyResultFailure(
                    errorMapping.mapFailure(exitStateModel.getErrorCode(), exitStateModel.getErrorMessage()));
        }

        RecurrentTokenIntent intent = recurrentResultIntentResolver.initIntentByStep(exitStateModel);

        return new RecurrentTokenProxyResult(intent)
                .setNextState(serializer.writeByte(exitStateModel))
                .setTrx(createTransactionInfo(
                        exitStateModel.getProviderTrxId(), exitStateModel.getTrxExtra())
                );
    }

}

