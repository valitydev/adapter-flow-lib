package dev.vality.adapter.flow.lib.converter;

import dev.vality.adapter.flow.lib.model.ExitStateModel;
import dev.vality.adapter.flow.lib.model.TemporaryContext;
import org.springframework.core.convert.converter.Converter;

public class ExitStateModelToTemporaryContextConverter implements Converter<ExitStateModel, TemporaryContext> {

    @Override
    public TemporaryContext convert(ExitStateModel source) {
        return TemporaryContext.builder()
                .providerTrxId(source.getProviderTrxId())
                .nextStep(source.getNextStep())
                .pollingInfo(source.getPollingInfo())
                .customContext(source.getCustomContext())
                .build();
    }

}
