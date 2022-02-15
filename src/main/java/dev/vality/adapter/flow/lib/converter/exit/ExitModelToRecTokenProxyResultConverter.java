package dev.vality.adapter.flow.lib.converter.exit;

import dev.vality.adapter.flow.lib.converter.ExitStateModelToTemporaryContextConverter;
import dev.vality.adapter.flow.lib.flow.RecurrentResultIntentResolver;
import dev.vality.adapter.flow.lib.model.ExitStateModel;
import dev.vality.adapter.flow.lib.serde.TemporaryContextSerializer;
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
        implements Converter<ExitStateModel, RecurrentTokenProxyResult> {

    private final ErrorMapping errorMapping;
    private final TemporaryContextSerializer serializer;
    private final RecurrentResultIntentResolver recurrentResultIntentResolver;
    private final ExitStateModelToTemporaryContextConverter contextConverter;

    @Override
    public RecurrentTokenProxyResult convert(ExitStateModel exitStateModel) {
        if (exitStateModel.getErrorCode() != null) {
            return createRecurrentTokenProxyResultFailure(
                    errorMapping.mapFailure(exitStateModel.getErrorCode(), exitStateModel.getErrorMessage()));
        }

        RecurrentTokenIntent intent = recurrentResultIntentResolver.initIntentByStep(exitStateModel);

        return new RecurrentTokenProxyResult(intent)
                .setNextState(serializer.writeByte(contextConverter.convert(exitStateModel)))
                .setTrx(createTransactionInfo(
                        exitStateModel.getProviderTrxId(), exitStateModel.getTrxExtra())
                );
    }

}

