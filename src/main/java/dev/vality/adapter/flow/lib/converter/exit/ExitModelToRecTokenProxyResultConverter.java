package dev.vality.adapter.flow.lib.converter.exit;

import dev.vality.adapter.flow.lib.converter.ExitStateModelToTemporaryContextConverter;
import dev.vality.adapter.flow.lib.flow.RecurrentResultIntentResolver;
import dev.vality.adapter.flow.lib.model.ExitStateModel;
import dev.vality.adapter.flow.lib.serde.TemporaryContextSerializer;
import dev.vality.adapter.flow.lib.service.factory.SimpleRecurrentIntentResultFactory;
import dev.vality.adapter.flow.lib.utils.AdditionalInfoUtils;
import dev.vality.damsel.domain.TransactionInfo;
import dev.vality.damsel.proxy_provider.RecurrentTokenIntent;
import dev.vality.damsel.proxy_provider.RecurrentTokenProxyResult;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.util.StringUtils;

import java.util.HashMap;

@RequiredArgsConstructor
public class ExitModelToRecTokenProxyResultConverter implements Converter<ExitStateModel, RecurrentTokenProxyResult> {

    private final SimpleRecurrentIntentResultFactory recurrentIntentResultFactory;
    private final TemporaryContextSerializer serializer;
    private final RecurrentResultIntentResolver recurrentResultIntentResolver;
    private final ExitStateModelToTemporaryContextConverter contextConverter;

    @Override
    public RecurrentTokenProxyResult convert(ExitStateModel exitStateModel) {
        if (exitStateModel.getErrorCode() != null) {
            return new RecurrentTokenProxyResult(
                    recurrentIntentResultFactory.createFinishIntentFailed(
                            exitStateModel.getErrorCode(),
                            exitStateModel.getErrorMessage()))
                    .setTrx(StringUtils.hasText(exitStateModel.getProviderTrxId())
                            ? getTransactionInfo(exitStateModel)
                            : null);
        }

        RecurrentTokenIntent intent = recurrentResultIntentResolver.initIntentByStep(exitStateModel);

        return new RecurrentTokenProxyResult(intent)
                .setNextState(serializer.writeByte(contextConverter.convert(exitStateModel)))
                .setTrx(getTransactionInfo(exitStateModel));
    }

    private TransactionInfo getTransactionInfo(ExitStateModel exitStateModel) {
        return new TransactionInfo()
                .setId(exitStateModel.getProviderTrxId())
                .setExtra(exitStateModel.getTrxExtra() != null
                        ? exitStateModel.getTrxExtra()
                        : new HashMap<>())
                .setAdditionalInfo(AdditionalInfoUtils.initAdditionalTrxInfo(exitStateModel));
    }
}

