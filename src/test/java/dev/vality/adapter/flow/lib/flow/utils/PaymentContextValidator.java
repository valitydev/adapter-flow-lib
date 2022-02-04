package dev.vality.adapter.flow.lib.flow.utils;

import dev.vality.adapter.common.Validator;
import dev.vality.damsel.proxy_provider.PaymentContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentContextValidator implements Validator<PaymentContext> {

    @Override
    public void validate(PaymentContext context) {

    }
}
