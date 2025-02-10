package dev.vality.adapter.flow.lib.handler;

import dev.vality.adapter.flow.lib.processor.Processor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;

import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public abstract class CommonHandlerImpl<P, R, E, T> implements CommonHandler<T, E> {

    private final Function<P, R> requestFunction;
    private final Converter<E, P> converter;
    private final Processor<T, R, E> processor;

    @Override
    public T handle(E entryStateModel) {
        P request = converter.convert(entryStateModel);
        R response = requestFunction.apply(request);
        return processor.process(response, entryStateModel);
    }

}
