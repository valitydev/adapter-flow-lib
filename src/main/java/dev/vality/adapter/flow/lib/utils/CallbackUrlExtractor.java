package dev.vality.adapter.flow.lib.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

import static dev.vality.adapter.common.constants.ThreeDsFields.TERM_URL;

@Component
@RequiredArgsConstructor
public class CallbackUrlExtractor {

    public static final String TERMINATION_URI = "termination_uri";

    private final AdapterProperties adapterProperties;

    public String extractCallbackUrl(Map<String, String> adapterConfigurations, String redirectUrl) {
        return UriComponentsBuilder.fromUriString(adapterProperties.getCallbackUrl())
                .path(adapterProperties.getPathCallbackUrl())
                .queryParam(TERMINATION_URI, getRedirectUrl(adapterConfigurations, redirectUrl)).build().toUriString();
    }

    private String getRedirectUrl(Map<String, String> adapterConfigurations, String redirectUrl) {
        if (StringUtils.hasText(redirectUrl)) {
            return redirectUrl;
        }
        return adapterConfigurations.getOrDefault(TERM_URL.getValue(), adapterProperties.getDefaultTermUrl());
    }

}
