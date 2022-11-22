package dev.vality.adapter.flow.lib.service.impl;

import dev.vality.adapter.flow.lib.constant.RedirectFields;
import dev.vality.adapter.flow.lib.service.CallbackUrlExtractor;
import dev.vality.adapter.flow.lib.utils.AdapterProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@RequiredArgsConstructor
public class CallbackUrlExtractorImpl implements CallbackUrlExtractor {

    private final AdapterProperties adapterProperties;

    @Override
    public String extractCallbackUrl(String redirectUrl) {
        return extractCallbackUrl(null, redirectUrl);
    }

    @Override
    public String extractCallbackUrl(Map<String, String> adapterConfigurations, String redirectUrl) {
        return UriComponentsBuilder.fromUriString(adapterProperties.getCallbackUrl())
                .path(adapterProperties.getPathCallbackUrl())
                .queryParam(TERMINATION_URI, redirectUrl).build().toUriString();
    }

    @Override
    public String getSuccessRedirectUrl(Map<String, String> adapterConfigurations, String redirectUrl) {
        if (StringUtils.hasText(redirectUrl)) {
            return redirectUrl;
        }
        return adapterConfigurations.getOrDefault(RedirectFields.TERM_URL.getValue(),
                adapterProperties.getSuccessRedirectUrl());
    }

}
