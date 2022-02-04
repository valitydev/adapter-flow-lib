package dev.vality.adapter.flow.lib.service;

import dev.vality.adapter.common.state.serializer.CallbackSerializer;
import dev.vality.adapter.flow.lib.model.ThreeDsV2Callback;
import dev.vality.adapter.flow.lib.utils.ThreeDsV2CallbackDeserializer;
import dev.vality.adapter.helpers.hellgate.HellgateAdapterClient;
import dev.vality.adapter.helpers.hellgate.exception.HellgateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.function.BiFunction;

@Slf4j
@Component
@RequiredArgsConstructor
public class ThreeDsV2AdapterService {

    private final HellgateAdapterClient hgClient;
    private final CallbackSerializer callbackSerializer;
    private final ThreeDsV2CallbackDeserializer callbackDeserializer;

    public String receivePaymentIncomingParameters(HttpServletRequest servletRequest) {
        return this.processCallback(servletRequest, hgClient::processPaymentCallback);
    }

    public String receiveRecurrentIncomingParameters(HttpServletRequest servletRequest) {
        return this.processCallback(servletRequest, hgClient::processRecurrentTokenCallback);
    }

    private String processCallback(HttpServletRequest servletRequest,
                                   BiFunction<String, ByteBuffer, ByteBuffer> hgFunction) {
        String resp = "";
        ThreeDsV2Callback callbackObj = this.callbackDeserializer.read(servletRequest);
        log.info("-> callback 3ds v2 {}", callbackObj);

        try {
            String tag = null;
            if (StringUtils.hasText(callbackObj.getThreeDSMethodData())) {
                tag = callbackObj.getThreeDSMethodData();
            } else {
                tag = callbackObj.getThreeDSSessionData();
            }
            ByteBuffer callback = ByteBuffer.wrap(this.callbackSerializer.writeByte(callbackObj));
            ByteBuffer response = hgFunction.apply(tag, callback);
            resp = new String(response.array(), StandardCharsets.UTF_8);
        } catch (HellgateException var9) {
            log.warn("Failed handle callback for recurrent", var9);
        } catch (Exception var10) {
            log.error("Failed handle callback for recurrent", var10);
        }

        log.info("<- callback 3ds v2 {}", resp);
        return resp;
    }

}
