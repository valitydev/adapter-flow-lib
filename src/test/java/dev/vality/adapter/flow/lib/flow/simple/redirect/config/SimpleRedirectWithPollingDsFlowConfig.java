package dev.vality.adapter.flow.lib.flow.simple.redirect.config;

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
import dev.vality.adapter.flow.lib.flow.simple.SimpleRedirectGenerateTokenResultIntentResolver;
import dev.vality.adapter.flow.lib.flow.simple.SimpleRedirectWithPollingResultIntentResolver;
import dev.vality.adapter.flow.lib.flow.simple.SimpleRedirectWithPollingStepResolverImpl;
import dev.vality.adapter.flow.lib.handler.CommonHandler;
import dev.vality.adapter.flow.lib.handler.ServerFlowHandler;
import dev.vality.adapter.flow.lib.handler.ServerFlowHandlerImpl;
import dev.vality.adapter.flow.lib.handler.payment.*;
import dev.vality.adapter.flow.lib.model.BaseResponseModel;
import dev.vality.adapter.flow.lib.model.EntryStateModel;
import dev.vality.adapter.flow.lib.model.ExitStateModel;
import dev.vality.adapter.flow.lib.processor.Processor;
import dev.vality.adapter.flow.lib.service.factory.SimpleIntentResultFactory;
import dev.vality.adapter.flow.lib.service.factory.SimpleRecurrentIntentResultFactory;
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
    public StepResolver<EntryStateModel, ExitStateModel> generateTokenStepResolverImpl() {
        return new GenerateTokenSimpleRedirectWithPollingStepResolverImpl();
    }

    @Bean
    public ServerFlowHandler<PaymentContext, PaymentProxyResult> serverFlowHandler(
            RemoteClient client,
            EntryModelToBaseRequestModelConverter entryModelToBaseRequestModelConverter,
            Processor<ExitStateModel, BaseResponseModel, EntryStateModel> baseProcessor,
            CtxToEntryModelConverter ctxToEntryModelConverter,
            ExitModelToProxyResultConverter exitModelToProxyResultConverter) {
        return new ServerFlowHandlerImpl<>(
                getHandlers(client, entryModelToBaseRequestModelConverter, baseProcessor),
                new SimpleRedirectWithPollingStepResolverImpl(),
                ctxToEntryModelConverter,
                exitModelToProxyResultConverter);
    }

    @Bean
    public RecurrentResultIntentResolver recurrentResultIntentResolver(
            SimpleRecurrentIntentResultFactory recurrentIntentResultFactory) {
        return new SimpleRedirectGenerateTokenResultIntentResolver(recurrentIntentResultFactory);
    }

    @Bean
    public ResultIntentResolver resultIntentResolver(SimpleIntentResultFactory intentResultFactory) {
        return new SimpleRedirectWithPollingResultIntentResolver(intentResultFactory);
    }

    @Bean
    public ServerFlowHandler<RecurrentTokenContext, RecurrentTokenProxyResult> generateTokenFlowHandler(
            RemoteClient client,
            EntryModelToBaseRequestModelConverter entryModelToBaseRequestModelConverter,
            Processor<ExitStateModel, BaseResponseModel, EntryStateModel> baseProcessor,
            StepResolver<EntryStateModel, ExitStateModel> generateTokenStepResolverImpl,
            RecCtxToEntryModelConverter recCtxToEntryStateModelConverter,
            ExitModelToRecTokenProxyResultConverter exitModelToRecTokenProxyResultConverter) {
        return new ServerFlowHandlerImpl<>(
                getHandlers(client, entryModelToBaseRequestModelConverter, baseProcessor),
                generateTokenStepResolverImpl,
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
                new GenerateTokenHandler(client, entryModelToBaseRequestModelConverter, baseProcessor),
                new RefundHandler(client, entryModelToBaseRequestModelConverter, baseProcessor));
    }
}
