package dev.vality.adapter.flow.lib.service;

import dev.vality.adapter.common.exception.HellgateException;
import dev.vality.adapter.common.hellgate.HellgateClient;
import dev.vality.adapter.flow.lib.serde.ParametersDeserializer;
import dev.vality.adapter.flow.lib.serde.ParametersSerializer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.BiFunction;

@Slf4j
@RequiredArgsConstructor
public class ThreeDsAdapterService {

    private final HellgateClient hellgateClient;
    private final ParametersSerializer parametersSerializer;
    private final ParametersDeserializer parametersDeserializer;
    private final TagManagementService tagManagementService;

    public String receivePaymentIncomingParameters(HttpServletRequest servletRequest,
                                                   HttpServletResponse servletResponse) {
        return this.processCallback(servletRequest, servletResponse, hellgateClient::processPaymentCallback);
    }

    public String receiveRecurrentIncomingParameters(HttpServletRequest servletRequest,
                                                     HttpServletResponse servletResponse) {
        return this.processCallback(servletRequest, servletResponse, hellgateClient::processRecurrentTokenCallback);
    }

    private String processCallback(HttpServletRequest servletRequest,
                                   HttpServletResponse servletResponse,
                                   BiFunction<String, ByteBuffer, ByteBuffer> hgFunction) {
        String resp = "";
        Map<String, String> parameters = this.parametersDeserializer.read(servletRequest);
        log.info("-> callback 3ds {}", parameters);

        try {
            ByteBuffer callback = ByteBuffer.wrap(this.parametersSerializer.writeByte(parameters));
            ByteBuffer response = hgFunction.apply(tagManagementService.findTag(parameters), callback);
            resp = new String(response.array(), StandardCharsets.UTF_8);
            if (StringUtils.hasText(parameters.get(CallbackUrlExtractor.TERMINATION_URI))) {
                servletResponse.sendRedirect(parameters.get(CallbackUrlExtractor.TERMINATION_URI));
            }
        } catch (HellgateException var9) {
            log.warn("Failed handle callback for recurrent", var9);
        } catch (Exception var10) {
            log.error("Failed handle callback for recurrent", var10);
        }

        log.info("<- callback 3ds {}", resp);
        return resp;
    }
}
