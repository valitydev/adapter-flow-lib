package dev.vality.adapter.flow.lib.flow.config;

import dev.vality.adapter.flow.lib.model.BaseResponseModel;
import dev.vality.adapter.flow.lib.model.EntryStateModel;
import dev.vality.adapter.flow.lib.model.ExitStateModel;
import dev.vality.adapter.flow.lib.processor.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ProcessorConfig {

    @Bean
    public Processor<ExitStateModel, BaseResponseModel, EntryStateModel> baseProcessor() {
        ErrorProcessor errorProcessor = new ErrorProcessor();
        SuccessFinishProcessor baseProcessor = new SuccessFinishProcessor(errorProcessor);
        RedirectProcessor redirectProcessor = new RedirectProcessor(baseProcessor);
        return new RetryProcessor(redirectProcessor);
    }

}
