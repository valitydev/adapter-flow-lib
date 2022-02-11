package dev.vality.adapter.flow.lib.converter.exit;

import dev.vality.adapter.flow.lib.converter.ExitStateModelToTemporaryContextConverter;
import dev.vality.adapter.flow.lib.model.ExitStateModel;
import dev.vality.adapter.flow.lib.service.ResultIntentResolver;
import dev.vality.adapter.flow.lib.utils.TemporaryContextSerializer;
import dev.vality.damsel.proxy_provider.Intent;
import dev.vality.damsel.proxy_provider.PaymentProxyResult;
import dev.vality.error.mapping.ErrorMapping;
import dev.vality.java.damsel.utils.creators.DomainPackageCreators;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import static dev.vality.java.damsel.utils.creators.ProxyProviderPackageCreators.createProxyResultFailure;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExitModelToProxyResultConverter implements Converter<ExitStateModel, PaymentProxyResult> {

    private final ErrorMapping errorMapping;
    private final TemporaryContextSerializer serializer;
    private final ResultIntentResolver resultIntentResolver;
    private final ExitStateModelToTemporaryContextConverter contextConverter;

    @Override
    public PaymentProxyResult convert(ExitStateModel exitStateModel) {
        //---error---
        if (StringUtils.hasText(exitStateModel.getErrorCode())) {
            return createProxyResultFailure(
                    errorMapping.mapFailure(exitStateModel.getErrorCode(), exitStateModel.getErrorMessage()));
        }

        Intent intent = resultIntentResolver.initIntentByStep(exitStateModel);

        return new PaymentProxyResult(intent)
                .setNextState(serializer.writeByte(contextConverter.convert(exitStateModel)))
                .setTrx(DomainPackageCreators.createTransactionInfo(
                        exitStateModel.getProviderTrxId(), exitStateModel.getTrxExtra())
                );
    }

}

