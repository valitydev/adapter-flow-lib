package dev.vality.adapter.flow.lib.flow.simple.redirect.config;

import dev.vality.adapter.common.handler.CommonHandler;
import dev.vality.adapter.common.processor.Processor;
import dev.vality.adapter.flow.lib.client.RemoteClient;
import dev.vality.adapter.flow.lib.converter.base.EntryModelToBaseRequestModelConverter;
import dev.vality.adapter.flow.lib.flow.RecurrentResultIntentResolver;
import dev.vality.adapter.flow.lib.flow.ResultIntentResolver;
import dev.vality.adapter.flow.lib.flow.full.RecurrentResultIntentResolverImpl;
import dev.vality.adapter.flow.lib.flow.full.ResultIntentResolverImpl;
import dev.vality.adapter.flow.lib.flow.simple.GenerateTokenSimpleRedirectWithPollingStepResolverImpl;
import dev.vality.adapter.flow.lib.flow.simple.SimpleRedirectRecurrentResultIntentResolver;
import dev.vality.adapter.flow.lib.flow.simple.SimpleRedirectWIthPollingStepResolverImpl;
import dev.vality.adapter.flow.lib.flow.StepResolver;
import dev.vality.adapter.flow.lib.flow.simple.SimpleRedirectWithPollingResultIntentResolver;
import dev.vality.adapter.flow.lib.handler.ServerFlowHandler;
import dev.vality.adapter.flow.lib.handler.payment.*;
import dev.vality.adapter.flow.lib.model.BaseResponseModel;
import dev.vality.adapter.flow.lib.model.EntryStateModel;
import dev.vality.adapter.flow.lib.model.ExitStateModel;
import dev.vality.adapter.flow.lib.service.TagManagementService;
import dev.vality.adapter.flow.lib.utils.CallbackUrlExtractor;
import dev.vality.adapter.flow.lib.utils.TimerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SimpleRedirectWithPollingDsFlowConfig {

    @Bean
    public StepResolver<EntryStateModel, ExitStateModel> stepResolverImpl() {
        return new SimpleRedirectWIthPollingStepResolverImpl();
    }

    @Bean
    public StepResolver<EntryStateModel, ExitStateModel> generateTokenStepResolverImpl() {
        return new GenerateTokenSimpleRedirectWithPollingStepResolverImpl();
    }

    @Bean
    public ServerFlowHandler serverFlowHandler(
            RemoteClient client,
            EntryModelToBaseRequestModelConverter entryModelToBaseRequestModelConverter,
            Processor<ExitStateModel, BaseResponseModel, EntryStateModel> baseProcessor,
            StepResolver<EntryStateModel, ExitStateModel> stepResolverImpl) {
        return new ServerFlowHandler(
                getHandlers(client, entryModelToBaseRequestModelConverter, baseProcessor),
                stepResolverImpl);
    }

    @Bean
    public RecurrentResultIntentResolver recurrentResultIntentResolver(
            TimerProperties timerProperties,
            CallbackUrlExtractor callbackUrlExtractor,
            TagManagementService tagManagementService) {
        return new SimpleRedirectRecurrentResultIntentResolver(timerProperties, callbackUrlExtractor, tagManagementService);
    }

    @Bean
    public ResultIntentResolver resultIntentResolver(
            TimerProperties timerProperties,
            CallbackUrlExtractor callbackUrlExtractor,
            TagManagementService tagManagementService) {
        return new SimpleRedirectWithPollingResultIntentResolver(timerProperties, callbackUrlExtractor, tagManagementService);
    }

    @Bean
    public ServerFlowHandler generateTokenFlowHandler(
            RemoteClient client,
            EntryModelToBaseRequestModelConverter entryModelToBaseRequestModelConverter,
            Processor<ExitStateModel, BaseResponseModel, EntryStateModel> baseProcessor,
            StepResolver<EntryStateModel, ExitStateModel> generateTokenStepResolverImpl) {
        return new ServerFlowHandler(
                getHandlers(client, entryModelToBaseRequestModelConverter, baseProcessor),
                generateTokenStepResolverImpl);
    }

    private List<CommonHandler<ExitStateModel, EntryStateModel>> getHandlers(
            RemoteClient client,
            EntryModelToBaseRequestModelConverter entryModelToBaseRequestModelConverter,
            Processor<ExitStateModel, BaseResponseModel, EntryStateModel> baseProcessor) {
        return List.of(new AuthHandler(client, entryModelToBaseRequestModelConverter, baseProcessor),
                new CancelHandler(client, entryModelToBaseRequestModelConverter, baseProcessor),
                new CaptureHandler(client, entryModelToBaseRequestModelConverter, baseProcessor),
                new StatusHandler(client, entryModelToBaseRequestModelConverter, baseProcessor),
                new DoNothingHandler(),
                new PaymentHandler(client, entryModelToBaseRequestModelConverter, baseProcessor),
                new RefundHandler(client, entryModelToBaseRequestModelConverter, baseProcessor));
    }
}
