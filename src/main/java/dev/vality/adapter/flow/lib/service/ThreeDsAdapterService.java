package dev.vality.adapter.flow.lib.service;

import dev.vality.adapter.flow.lib.utils.ParameterSerializer;
import dev.vality.adapter.flow.lib.utils.ParametersDeserializer;
import dev.vality.adapter.helpers.hellgate.HellgateAdapterClient;
import dev.vality.adapter.helpers.hellgate.exception.HellgateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.BiFunction;

@Slf4j
@Component
@RequiredArgsConstructor
public class ThreeDsAdapterService {

    private final HellgateAdapterClient hgClient;
    private final ParameterSerializer parameterSerializer;
    private final ParametersDeserializer parametersDeserializer;
    private final TagManagementService tagManagementService;

    public String receivePaymentIncomingParameters(HttpServletRequest servletRequest) {
        return this.processCallback(servletRequest, hgClient::processPaymentCallback);
    }

    public String receiveRecurrentIncomingParameters(HttpServletRequest servletRequest) {
        return this.processCallback(servletRequest, hgClient::processRecurrentTokenCallback);
    }

    private String processCallback(HttpServletRequest servletRequest,
                                   BiFunction<String, ByteBuffer, ByteBuffer> hgFunction) {
        String resp = "";
        Map<String, String> parameters = this.parametersDeserializer.read(servletRequest);
        log.info("-> callback 3ds {}", parameters);

        try {
            ByteBuffer callback = ByteBuffer.wrap(this.parameterSerializer.writeByte(parameters));
            ByteBuffer response = hgFunction.apply(tagManagementService.findTag(parameters), callback);
            resp = new String(response.array(), StandardCharsets.UTF_8);
        } catch (HellgateException var9) {
            log.warn("Failed handle callback for recurrent", var9);
        } catch (Exception var10) {
            log.error("Failed handle callback for recurrent", var10);
        }

        log.info("<- callback 3ds {}", resp);
        return resp;
    }

}
