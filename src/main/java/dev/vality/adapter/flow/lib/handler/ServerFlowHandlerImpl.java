package dev.vality.adapter.flow.lib.handler;

import dev.vality.adapter.flow.lib.exception.UnknownHandlerForStepException;
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
public class ServerFlowHandlerImpl<T, R> implements ServerFlowHandler<T, R> {

    private final List<CommonHandler<ExitStateModel, EntryStateModel>> handlers;
    private final StepResolver<EntryStateModel, ExitStateModel> stepResolver;
    private final Converter<T, EntryStateModel> entryConverter;
    private final Converter<ExitStateModel, R> exitConverter;

    @Override
    public R handle(T context) throws TException {
        var entryStateModel = prepareEntryState(entryConverter, context);
        log.info("EntryStateModel: {}", entryStateModel);
        var exitStateModel = handlers.stream()
                .filter(handler -> handler.isHandle(entryStateModel))
                .findFirst()
                .orElseThrow(() -> new UnknownHandlerForStepException("Can't find handler to data: " + entryStateModel))
                .handle(entryStateModel);
        log.info("ExitStateModel: {}", exitStateModel);
        exitStateModel.setEntryStateModel(entryStateModel);
        exitStateModel.setNextStep(stepResolver.resolveNextStep(exitStateModel));
        log.info("Step changing: {} -> {}", entryStateModel.getCurrentStep(), exitStateModel.getNextStep());
        return exitConverter.convert(exitStateModel);
    }

    private EntryStateModel prepareEntryState(Converter<T, EntryStateModel> entryConverter, T context) {
        EntryStateModel entryStateModel = entryConverter.convert(context);
        entryStateModel.setCurrentStep(stepResolver.resolveCurrentStep(entryStateModel));
        return entryStateModel;
    }
}
