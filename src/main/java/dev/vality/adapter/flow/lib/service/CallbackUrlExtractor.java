package dev.vality.adapter.flow.lib.service;

import java.util.Map;

public interface CallbackUrlExtractor {

    String TERMINATION_URI = "termination_uri";

    @Deprecated
    String extractCallbackUrl(String redirectUrl);

    String extractCallbackUrl(Map<String, String> adapterConfigurations, String redirectUrl);

    String getSuccessRedirectUrl(Map<String, String> adapterConfigurations, String redirectUrl);

}
