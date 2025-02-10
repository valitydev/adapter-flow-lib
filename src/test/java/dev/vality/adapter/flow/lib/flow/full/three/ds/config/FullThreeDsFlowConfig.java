package dev.vality.adapter.flow.lib.flow.full.three.ds.config;

import dev.vality.adapter.flow.lib.client.RemoteClient;
import dev.vality.adapter.flow.lib.converter.base.EntryModelToBaseRequestModelConverter;
import dev.vality.adapter.flow.lib.converter.entry.CtxToEntryModelConverter;
import dev.vality.adapter.flow.lib.converter.entry.RecCtxToEntryModelConverter;
import dev.vality.adapter.flow.lib.converter.exit.ExitModelToProxyResultConverter;
import dev.vality.adapter.flow.lib.converter.exit.ExitModelToRecTokenProxyResultConverter;
import dev.vality.adapter.flow.lib.flow.RecurrentResultIntentResolver;
import dev.vality.adapter.flow.lib.flow.ResultIntentResolver;
import dev.vality.adapter.flow.lib.flow.StepResolver;
import dev.vality.adapter.flow.lib.flow.full.FullThreeDsAllVersionsStepResolverImpl;
import dev.vality.adapter.flow.lib.flow.full.GenerateTokenFullThreeDsAllVersionsStepResolverImpl;
import dev.vality.adapter.flow.lib.flow.full.GenerateTokenResultIntentResolverImpl;
import dev.vality.adapter.flow.lib.flow.full.ResultIntentResolverImpl;
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
public class FullThreeDsFlowConfig {

    @Bean
    public StepResolver<EntryStateModel, ExitStateModel> fullThreeDsAllVersionsStepResolverImpl() {
        return new FullThreeDsAllVersionsStepResolverImpl();
    }


    @Bean
    public StepResolver<EntryStateModel, ExitStateModel> generateTokenFullThreeDsAllVersionsStepResolverImpl() {
        return new GenerateTokenFullThreeDsAllVersionsStepResolverImpl();
    }

    @Bean
    public ServerFlowHandler<PaymentContext, PaymentProxyResult> serverFlowHandler(
            RemoteClient client,
            EntryModelToBaseRequestModelConverter entryModelToBaseRequestModelConverter,
            Processor<ExitStateModel, BaseResponseModel, EntryStateModel> baseProcessor,
            StepResolver<EntryStateModel, ExitStateModel> fullThreeDsAllVersionsStepResolverImpl,
            CtxToEntryModelConverter ctxToEntryModelConverter,
            ExitModelToProxyResultConverter exitModelToProxyResultConverter) {
        return new ServerFlowHandlerImpl<>(
                getHandlers(client, entryModelToBaseRequestModelConverter, baseProcessor),
                fullThreeDsAllVersionsStepResolverImpl,
                ctxToEntryModelConverter,
                exitModelToProxyResultConverter);
    }

    @Bean
    public ServerFlowHandler<RecurrentTokenContext, RecurrentTokenProxyResult> generateTokenFlowHandler(
            RemoteClient client,
            EntryModelToBaseRequestModelConverter entryModelToBaseRequestModelConverter,
            Processor<ExitStateModel, BaseResponseModel, EntryStateModel> baseProcessor,
            StepResolver<EntryStateModel, ExitStateModel> generateTokenFullThreeDsAllVersionsStepResolverImpl,
            RecCtxToEntryModelConverter recCtxToEntryStateModelConverter,
            ExitModelToRecTokenProxyResultConverter exitModelToRecTokenProxyResultConverter
    ) {
        return new ServerFlowHandlerImpl<>(
                getHandlers(client, entryModelToBaseRequestModelConverter, baseProcessor),
                generateTokenFullThreeDsAllVersionsStepResolverImpl,
                recCtxToEntryStateModelConverter,
                exitModelToRecTokenProxyResultConverter);
    }

    @Bean
    public RecurrentResultIntentResolver recurrentResultIntentResolver(
            SimpleRecurrentIntentResultFactory recurrentIntentResultFactory) {
        return new GenerateTokenResultIntentResolverImpl(recurrentIntentResultFactory);
    }

    @Bean
    public ResultIntentResolver resultIntentResolver(SimpleIntentResultFactory intentResultFactory) {
        return new ResultIntentResolverImpl(intentResultFactory);
    }

    private List<CommonHandler<ExitStateModel, EntryStateModel>> getHandlers(
            RemoteClient client,
            EntryModelToBaseRequestModelConverter entryModelToBaseRequestModelConverter,
            Processor<ExitStateModel, BaseResponseModel, EntryStateModel> baseProcessor) {
        return List.of(new AuthHandler(client, entryModelToBaseRequestModelConverter, baseProcessor),
                new CancelHandler(client, entryModelToBaseRequestModelConverter, baseProcessor),
                new CaptureHandler(client, entryModelToBaseRequestModelConverter, baseProcessor),
                new Check3dsV2Handler(client, entryModelToBaseRequestModelConverter, baseProcessor),
                new DoNothingHandler(),
                new Finish3dsHandler(client, entryModelToBaseRequestModelConverter, baseProcessor),
                new Finish3dsV2Handler(client, entryModelToBaseRequestModelConverter, baseProcessor),
                new PaymentHandler(client, entryModelToBaseRequestModelConverter, baseProcessor),
                new RefundHandler(client, entryModelToBaseRequestModelConverter, baseProcessor));
    }
}
