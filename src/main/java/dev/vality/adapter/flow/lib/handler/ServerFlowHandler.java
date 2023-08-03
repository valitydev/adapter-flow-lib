package dev.vality.adapter.flow.lib.handler;

import org.apache.thrift.TException;

public interface ServerFlowHandler<T, R> {

    default R handle(T context) throws TException {
        throw new UnsupportedOperationException();
    }

}
