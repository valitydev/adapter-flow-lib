package dev.vality.adapter.flow.lib.handler;

import dev.vality.adapter.common.Validator;
import dev.vality.adapter.common.exception.UnsupportedMethodException;
import dev.vality.adapter.common.handler.CommonHandler;
import dev.vality.adapter.flow.lib.flow.StepResolver;
import dev.vality.adapter.flow.lib.model.GeneralEntryStateModel;
import dev.vality.adapter.flow.lib.model.GeneralExitStateModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.core.convert.converter.Converter;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class ServerFlowHandler {

    private final List<CommonHandler<GeneralExitStateModel, GeneralEntryStateModel>> handlers;
    private final StepResolver<GeneralEntryStateModel, GeneralExitStateModel> stepResolver;

    public <T, R> R handle(Validator<T> validator,
                           Converter<T, GeneralEntryStateModel> entryConverter,
                           Converter<GeneralExitStateModel, R> exitConverter,
                           T context) throws TException {
        GeneralEntryStateModel entryStateModel = prepareEntryState(validator, entryConverter, context);
        log.info("EntryStateModel: {}", entryStateModel);
        GeneralExitStateModel exitStateModel = handlers.stream()
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

    private <T> GeneralEntryStateModel prepareEntryState(Validator<T> validator,
                                                         Converter<T, GeneralEntryStateModel> entryConverter,
                                                         T context) {
        validator.validate(context);
        GeneralEntryStateModel entryStateModel = entryConverter.convert(context);
        entryStateModel.setCurrentStep(stepResolver.resolveEntry(entryStateModel));
        return entryStateModel;
    }
}
