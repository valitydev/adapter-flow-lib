package dev.vality.adapter.flow.lib.utils;

import dev.vality.adapter.flow.lib.constant.RedirectFields;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@RequiredArgsConstructor
public class CallbackUrlExtractor {

    public static final String TERMINATION_URI = "termination_uri";

    private final AdapterProperties adapterProperties;

    public String extractCallbackUrl(String redirectUrl) {
        return UriComponentsBuilder.fromUriString(adapterProperties.getCallbackUrl())
                .path(adapterProperties.getPathCallbackUrl())
                .queryParam(TERMINATION_URI, redirectUrl).build().toUriString();
    }

    public String getSuccessRedirectUrl(Map<String, String> adapterConfigurations, String redirectUrl) {
        if (StringUtils.hasText(redirectUrl)) {
            return redirectUrl;
        }
        return adapterConfigurations.getOrDefault(RedirectFields.TERM_URL.getValue(),
                adapterProperties.getSuccessRedirectUrl());
    }

}
