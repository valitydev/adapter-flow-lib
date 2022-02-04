package dev.vality.adapter.flow.lib.controller;

import dev.vality.adapter.common.controller.AdapterController;
import dev.vality.adapter.common.state.deserializer.CallbackDeserializer;
import dev.vality.adapter.common.state.serializer.CallbackSerializer;
import dev.vality.adapter.helpers.hellgate.HellgateAdapterClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/${server.rest.endpoint}/three_ds_v1/")
public class ThreeDsV1CallbackController {

    private AdapterController adapterController;

    public ThreeDsV1CallbackController(
            HellgateAdapterClient hgClient,
            CallbackSerializer callbackSerializer,
            CallbackDeserializer callbackDeserializer
    ) {
        adapterController = new AdapterController(hgClient, callbackSerializer, callbackDeserializer);
    }

    @PostMapping(value = "term_url")
    public String receivePaymentIncomingParameters(HttpServletRequest servletRequest,
                                                   HttpServletResponse servletResponse) throws IOException {
        return adapterController.receivePaymentIncomingParameters(servletRequest, servletResponse);
    }

    @PostMapping(value = "rec_term_url")
    public String receiveRecurrentIncomingParameters(HttpServletRequest servletRequest,
                                                     HttpServletResponse servletResponse) throws IOException {
        return adapterController.receiveRecurrentIncomingParameters(servletRequest, servletResponse);
    }

}