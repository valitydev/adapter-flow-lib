package dev.vality.adapter.flow.lib.flow.config;

import dev.vality.adapter.common.processor.Processor;
import dev.vality.adapter.flow.lib.model.BaseResponseModel;
import dev.vality.adapter.flow.lib.model.GeneralEntryStateModel;
import dev.vality.adapter.flow.lib.model.GeneralExitStateModel;
import dev.vality.adapter.flow.lib.processor.BaseProcessor;
import dev.vality.adapter.flow.lib.processor.ErrorProcessor;
import dev.vality.adapter.flow.lib.processor.RedirectProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ProcessorConfig {

    @Bean
    public Processor<GeneralExitStateModel, BaseResponseModel, GeneralEntryStateModel> baseProcessor() {
        ErrorProcessor errorProcessor = new ErrorProcessor();
        BaseProcessor baseProcessor = new BaseProcessor(errorProcessor);
        return new RedirectProcessor(baseProcessor);
    }

}
