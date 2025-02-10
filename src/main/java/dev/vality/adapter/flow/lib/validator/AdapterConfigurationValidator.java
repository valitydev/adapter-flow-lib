package dev.vality.adapter.flow.lib.validator;

import java.util.Map;

/**
 * Used to check adapter configuration settings.
 */
public interface AdapterConfigurationValidator {

    /**
     * @param adapterConfigurationsParameters map of parameters that are configured by supports,
     *                                        uniq for adapter implementation
     */
    void validate(Map<String, String> adapterConfigurationsParameters);

}
