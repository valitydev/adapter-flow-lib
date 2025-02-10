package dev.vality.adapter.flow.lib.processor;

public interface Processor<R, T, E> {

    R process(T response, E context);

}
