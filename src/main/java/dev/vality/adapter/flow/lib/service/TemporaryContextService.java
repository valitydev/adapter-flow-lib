package dev.vality.adapter.flow.lib.service;

import dev.vality.adapter.flow.lib.model.TemporaryContext;
import dev.vality.adapter.flow.lib.serde.Deserializer;
import dev.vality.adapter.flow.lib.serde.ParametersDeserializer;
import dev.vality.damsel.proxy_provider.PaymentContext;
import dev.vality.damsel.proxy_provider.RecurrentTokenContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class TemporaryContextService {

    private final ParametersDeserializer parametersDeserializer;

    public TemporaryContext getTemporaryContext(Object context, Deserializer<TemporaryContext> deserializer) {
        byte[] state = getState(context);
        if (state != null && state.length > 0) {
            return deserializer.read(state);
        }
        return new TemporaryContext();
    }

    public TemporaryContext appendThreeDsParametersToContext(ByteBuffer callback, TemporaryContext temporaryContext) {
        if (callback != null && callback.hasArray() && callback.array().length > 0) {
            Map<String, String> parameters = parametersDeserializer.read(callback.array());
            if (temporaryContext != null) {
                temporaryContext.setThreeDsData(parameters);
            } else {
                if (parameters == null || parameters.isEmpty()) {
                    throw new RuntimeException("Unknown parameters or baseModel!");
                }
            }
        }
        log.info("TemporaryContext: {} after callback.", temporaryContext);
        return temporaryContext;

    }

    private static byte[] getState(Object context) {
        if (context instanceof RecurrentTokenContext) {
            if (((RecurrentTokenContext) context).getSession() == null) {
                return new byte[0];
            }
            return ((RecurrentTokenContext) context).getSession().getState();
        }
        return ((PaymentContext) context).getSession().getState();
    }

}