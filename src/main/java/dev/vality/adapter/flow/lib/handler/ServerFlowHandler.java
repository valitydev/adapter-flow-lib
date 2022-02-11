package dev.vality.adapter.flow.lib.handler;

import dev.vality.adapter.common.Validator;
import dev.vality.adapter.common.handler.CommonHandler;
import dev.vality.adapter.flow.lib.flow.StepResolver;
import dev.vality.adapter.flow.lib.model.EntryStateModel;
import dev.vality.adapter.flow.lib.model.ExitStateModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.core.convert.converter.Converter;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class ServerFlowHandler {

    private final List<CommonHandler<ExitStateModel, EntryStateModel>> handlers;
    private final StepResolver<EntryStateModel, ExitStateModel> stepResolver;

    public <T, R> R handle(Validator<T> validator,
                           Converter<T, EntryStateModel> entryConverter,
                           Converter<ExitStateModel, R> exitConverter,
                           T context) throws TException {
        EntryStateModel entryStateModel = prepareEntryState(validator, entryConverter, context);
        log.info("EntryStateModel: {}", entryStateModel);
        ExitStateModel exitStateModel = handlers.stream()
                .filter(handler -> handler.isHandle(entryStateModel))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("" + entryStateModel))
                .handle(entryStateModel);
        log.info("ExitStateModel: {}", exitStateModel);
        exitStateModel.setNextStep(stepResolver.resolveExit(exitStateModel));
        log.info("Step changing: {} -> {}",
                entryStateModel.getCurrentStep(), exitStateModel.getNextStep());
        return exitConverter.convert(exitStateModel);
    }

    private <T> EntryStateModel prepareEntryState(Validator<T> validator,
                                                  Converter<T, EntryStateModel> entryConverter,
                                                  T context) {
        validator.validate(context);
        EntryStateModel entryStateModel = entryConverter.convert(context);
        entryStateModel.setCurrentStep(stepResolver.resolveEntry(entryStateModel));
        return entryStateModel;
    }
}
