package dev.vality.adapter.flow.lib.service;

import java.util.Map;

public interface CallbackUrlExtractor {

    String TERMINATION_URI = "termination_uri";

    String extractCallbackUrl(String redirectUrl);

    String getSuccessRedirectUrl(Map<String, String> adapterConfigurations, String redirectUrl);

}
