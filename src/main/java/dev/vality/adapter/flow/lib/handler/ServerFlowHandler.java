package dev.vality.adapter.flow.lib.handler;

import dev.vality.adapter.flow.lib.flow.StepResolver;
import dev.vality.adapter.flow.lib.model.EntryStateModel;
import dev.vality.adapter.flow.lib.model.ExitStateModel;
import dev.vality.adapter.flow.lib.validator.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.core.convert.converter.Converter;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class ServerFlowHandler<T, R> {

    private final List<CommonHandler<ExitStateModel, EntryStateModel>> handlers;
    private final StepResolver<EntryStateModel, ExitStateModel> stepResolver;
    private final Validator<T> validator;
    private final Converter<T, EntryStateModel> entryConverter;
    private final Converter<ExitStateModel, R> exitConverter;

    public R handle(T context) throws TException {
        EntryStateModel entryStateModel = prepareEntryState(validator, entryConverter, context);
        log.info("EntryStateModel: {}", entryStateModel);
        ExitStateModel exitStateModel = handlers.stream()
                .filter(handler -> handler.isHandle(entryStateModel))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("" + entryStateModel))
                .handle(entryStateModel);
        log.info("ExitStateModel: {}", exitStateModel);
        exitStateModel.setGeneralEntryStateModel(entryStateModel);
        exitStateModel.setNextStep(stepResolver.resolveNextStep(exitStateModel));
        log.info("Step changing: {} -> {}",
                entryStateModel.getCurrentStep(), exitStateModel.getNextStep());
        return exitConverter.convert(exitStateModel);
    }

    private EntryStateModel prepareEntryState(Validator<T> validator,
                                              Converter<T, EntryStateModel> entryConverter,
                                              T context) {
        validator.validate(context);
        EntryStateModel entryStateModel = entryConverter.convert(context);
        entryStateModel.setCurrentStep(stepResolver.resolveCurrentStep(entryStateModel));
        return entryStateModel;
    }
}
