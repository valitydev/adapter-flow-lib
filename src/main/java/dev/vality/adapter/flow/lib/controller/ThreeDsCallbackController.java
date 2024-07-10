package dev.vality.adapter.flow.lib.controller;

import dev.vality.adapter.flow.lib.service.ThreeDsAdapterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping({"/${server.rest.endpoint}"})
@RequiredArgsConstructor
public class ThreeDsCallbackController {

    private final ThreeDsAdapterService threeDsAdapterService;

    @PostMapping({"term-url"})
    public String receivePaymentIncomingParameters(HttpServletRequest servletRequest,
                                                   HttpServletResponse servletResponse) {
        return this.threeDsAdapterService.receivePaymentIncomingParameters(servletRequest, servletResponse);
    }

    @PostMapping({"recurrent-term-url"})
    public String receiveRecurrentIncomingParameters(HttpServletRequest servletRequest,
                                                     HttpServletResponse servletResponse) {
        return this.threeDsAdapterService.receiveRecurrentIncomingParameters(servletRequest, servletResponse);
    }
}