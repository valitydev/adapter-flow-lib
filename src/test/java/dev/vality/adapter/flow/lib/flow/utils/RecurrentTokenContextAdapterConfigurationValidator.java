package dev.vality.adapter.flow.lib.flow.utils;

import dev.vality.adapter.flow.lib.validator.AdapterConfigurationValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class RecurrentTokenContextAdapterConfigurationValidator implements AdapterConfigurationValidator {

    @Override
    public void validate(Map<String, String> context) {

    }

}
