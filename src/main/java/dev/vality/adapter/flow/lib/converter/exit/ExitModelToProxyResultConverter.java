package dev.vality.adapter.flow.lib.converter.exit;

import dev.vality.adapter.flow.lib.model.GeneralExitStateModel;
import dev.vality.adapter.flow.lib.service.ResultIntentResolver;
import dev.vality.adapter.flow.lib.utils.AdapterSerializer;
import dev.vality.damsel.proxy_provider.Intent;
import dev.vality.damsel.proxy_provider.PaymentProxyResult;
import dev.vality.error.mapping.ErrorMapping;
import dev.vality.java.damsel.utils.creators.DomainPackageCreators;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

import static dev.vality.java.damsel.utils.creators.ProxyProviderPackageCreators.createProxyResultFailure;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExitModelToProxyResultConverter implements Converter<GeneralExitStateModel, PaymentProxyResult> {

    private final ErrorMapping errorMapping;
    private final AdapterSerializer adapterSerializer;
    private final ResultIntentResolver resultIntentResolver;

    @Override
    public PaymentProxyResult convert(GeneralExitStateModel exitStateModel) {
        //---error---
        if (StringUtils.hasText(exitStateModel.getErrorCode())) {
            return createProxyResultFailure(
                    errorMapping.mapFailure(exitStateModel.getErrorCode(), exitStateModel.getErrorMessage()));
        }

        Intent intent = resultIntentResolver.initIntentByStep(exitStateModel);

        return new PaymentProxyResult(intent)
                .setNextState(adapterSerializer.writeByte(exitStateModel))
                .setTrx(DomainPackageCreators.createTransactionInfo(
                        exitStateModel.getProviderTrxId(), exitStateModel.getTrxExtra())
                );
    }

}

