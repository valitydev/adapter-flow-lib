package dev.vality.adapter.flow.lib.handler;

import org.apache.thrift.TException;

public interface CommonHandler<T, E> {

    boolean isHandle(E model);

    T handle(E context) throws TException;

}
