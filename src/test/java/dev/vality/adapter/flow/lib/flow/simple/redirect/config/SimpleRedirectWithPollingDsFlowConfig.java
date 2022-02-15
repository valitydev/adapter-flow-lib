package dev.vality.adapter.flow.lib.flow.simple.redirect.config;

import dev.vality.adapter.common.handler.CommonHandler;
import dev.vality.adapter.common.processor.Processor;
import dev.vality.adapter.flow.lib.client.RemoteClient;
import dev.vality.adapter.flow.lib.converter.base.EntryModelToBaseRequestModelConverter;
import dev.vality.adapter.flow.lib.converter.entry.CtxToEntryModelConverter;
import dev.vality.adapter.flow.lib.converter.entry.RecCtxToEntryModelConverter;
import dev.vality.adapter.flow.lib.converter.exit.ExitModelToProxyResultConverter;
import dev.vality.adapter.flow.lib.converter.exit.ExitModelToRecTokenProxyResultConverter;
import dev.vality.adapter.flow.lib.flow.RecurrentResultIntentResolver;
import dev.vality.adapter.flow.lib.flow.ResultIntentResolver;
import dev.vality.adapter.flow.lib.flow.StepResolver;
import dev.vality.adapter.flow.lib.flow.simple.GenerateTokenSimpleRedirectWithPollingStepResolverImpl;
import dev.vality.adapter.flow.lib.flow.simple.SimpleRedirectRecurrentResultIntentResolver;
import dev.vality.adapter.flow.lib.flow.simple.SimpleRedirectWIthPollingStepResolverImpl;
import dev.vality.adapter.flow.lib.flow.simple.SimpleRedirectWithPollingResultIntentResolver;
import dev.vality.adapter.flow.lib.flow.utils.PaymentContextValidator;
import dev.vality.adapter.flow.lib.flow.utils.RecurrentTokenContextValidator;
import dev.vality.adapter.flow.lib.handler.ServerFlowHandler;
import dev.vality.adapter.flow.lib.handler.payment.*;
import dev.vality.adapter.flow.lib.model.BaseResponseModel;
import dev.vality.adapter.flow.lib.model.EntryStateModel;
import dev.vality.adapter.flow.lib.model.ExitStateModel;
import dev.vality.adapter.flow.lib.service.TagManagementService;
import dev.vality.adapter.flow.lib.utils.CallbackUrlExtractor;
import dev.vality.adapter.flow.lib.utils.TimerProperties;
import dev.vality.damsel.proxy_provider.PaymentContext;
import dev.vality.damsel.proxy_provider.PaymentProxyResult;
import dev.vality.damsel.proxy_provider.RecurrentTokenContext;
import dev.vality.damsel.proxy_provider.RecurrentTokenProxyResult;
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
    public ServerFlowHandler<PaymentContext, PaymentProxyResult> serverFlowHandler(
            RemoteClient client,
            EntryModelToBaseRequestModelConverter entryModelToBaseRequestModelConverter,
            Processor<ExitStateModel, BaseResponseModel, EntryStateModel> baseProcessor,
            StepResolver<EntryStateModel, ExitStateModel> stepResolverImpl,
            PaymentContextValidator paymentContextValidator,
            CtxToEntryModelConverter ctxToEntryModelConverter,
            ExitModelToProxyResultConverter exitModelToProxyResultConverter) {
        return new ServerFlowHandler<>(
                getHandlers(client, entryModelToBaseRequestModelConverter, baseProcessor),
                stepResolverImpl,
                paymentContextValidator,
                ctxToEntryModelConverter,
                exitModelToProxyResultConverter);
    }

    @Bean
    public RecurrentResultIntentResolver recurrentResultIntentResolver(
            TimerProperties timerProperties,
            CallbackUrlExtractor callbackUrlExtractor,
            TagManagementService tagManagementService) {
        return new SimpleRedirectRecurrentResultIntentResolver(timerProperties,
                callbackUrlExtractor,
                tagManagementService);
    }

    @Bean
    public ResultIntentResolver resultIntentResolver(
            TimerProperties timerProperties,
            CallbackUrlExtractor callbackUrlExtractor,
            TagManagementService tagManagementService) {
        return new SimpleRedirectWithPollingResultIntentResolver(timerProperties,
                callbackUrlExtractor,
                tagManagementService);
    }

    @Bean
    public ServerFlowHandler<RecurrentTokenContext, RecurrentTokenProxyResult> generateTokenFlowHandler(
            RemoteClient client,
            EntryModelToBaseRequestModelConverter entryModelToBaseRequestModelConverter,
            Processor<ExitStateModel, BaseResponseModel, EntryStateModel> baseProcessor,
            StepResolver<EntryStateModel, ExitStateModel> generateTokenStepResolverImpl,
            RecurrentTokenContextValidator recurrentTokenContextValidator,
            RecCtxToEntryModelConverter recCtxToEntryStateModelConverter,
            ExitModelToRecTokenProxyResultConverter exitModelToRecTokenProxyResultConverter) {
        return new ServerFlowHandler<>(
                getHandlers(client, entryModelToBaseRequestModelConverter, baseProcessor),
                generateTokenStepResolverImpl,
                recurrentTokenContextValidator,
                recCtxToEntryStateModelConverter,
                exitModelToRecTokenProxyResultConverter);
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
