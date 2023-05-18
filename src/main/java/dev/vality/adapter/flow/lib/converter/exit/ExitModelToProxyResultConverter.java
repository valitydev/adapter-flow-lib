package dev.vality.adapter.flow.lib.converter.exit;

import dev.vality.adapter.flow.lib.converter.ExitStateModelToTemporaryContextConverter;
import dev.vality.adapter.flow.lib.flow.ResultIntentResolver;
import dev.vality.adapter.flow.lib.model.ExitStateModel;
import dev.vality.adapter.flow.lib.serde.TemporaryContextSerializer;
import dev.vality.adapter.flow.lib.service.factory.IntentResultFactory;
import dev.vality.adapter.flow.lib.utils.AdditionalInfoUtils;
import dev.vality.damsel.domain.TransactionInfo;
import dev.vality.damsel.proxy_provider.Intent;
import dev.vality.damsel.proxy_provider.PaymentProxyResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.util.StringUtils;

import java.util.HashMap;

@Slf4j
@RequiredArgsConstructor
public class ExitModelToProxyResultConverter implements Converter<ExitStateModel, PaymentProxyResult> {

    private final IntentResultFactory intentResultFactory;
    private final TemporaryContextSerializer serializer;
    private final ResultIntentResolver resultIntentResolver;
    private final ExitStateModelToTemporaryContextConverter contextConverter;

    @Override
    public PaymentProxyResult convert(ExitStateModel exitStateModel) {
        if (StringUtils.hasText(exitStateModel.getErrorCode())) {
            return new PaymentProxyResult(intentResultFactory.createFinishIntentFailed(exitStateModel))
                    .setTrx(StringUtils.hasText(exitStateModel.getProviderTrxId())
                            ? getTransactionInfo(exitStateModel)
                            : null);
        }

        Intent intent = resultIntentResolver.initIntentByStep(exitStateModel);

        return new PaymentProxyResult(intent)
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

