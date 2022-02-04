package dev.vality.adapter.flow.lib.controller;

import dev.vality.adapter.flow.lib.service.ThreeDsV2AdapterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping({"/${server.rest.endpoint}/three_ds_v2/"})
@RequiredArgsConstructor
public class ThreeDsV2CallbackController {

    private final ThreeDsV2AdapterService threeDsV2AdapterService;

    @PostMapping({"term_url"})
    public String receivePaymentIncomingParameters(HttpServletRequest servletRequest) {
        return this.threeDsV2AdapterService.receivePaymentIncomingParameters(servletRequest);
    }

    @PostMapping({"rec_term_url"})
    public String receiveRecurrentIncomingParameters(HttpServletRequest servletRequest) {
        return this.threeDsV2AdapterService.receiveRecurrentIncomingParameters(servletRequest);
    }
}