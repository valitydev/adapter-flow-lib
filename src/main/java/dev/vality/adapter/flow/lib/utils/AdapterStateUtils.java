package dev.vality.adapter.flow.lib.utils;

import dev.vality.adapter.common.state.deserializer.Deserializer;
import dev.vality.adapter.flow.lib.model.TemporaryContext;
import dev.vality.damsel.proxy_provider.PaymentContext;
import dev.vality.damsel.proxy_provider.RecurrentTokenContext;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AdapterStateUtils {

    public static TemporaryContext getTemporaryContext(Object context, Deserializer<TemporaryContext> deserializer) {
        byte[] state = getState(context);
        if (state != null && state.length > 0) {
            return deserializer.read(state);
        }
        return new TemporaryContext();
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