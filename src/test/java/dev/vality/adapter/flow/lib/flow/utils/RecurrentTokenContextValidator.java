package dev.vality.adapter.flow.lib.flow.utils;

import dev.vality.adapter.flow.lib.validator.Validator;
import dev.vality.damsel.proxy_provider.RecurrentTokenContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecurrentTokenContextValidator implements Validator<RecurrentTokenContext> {

    @Override
    public void validate(RecurrentTokenContext context) {

    }
}
