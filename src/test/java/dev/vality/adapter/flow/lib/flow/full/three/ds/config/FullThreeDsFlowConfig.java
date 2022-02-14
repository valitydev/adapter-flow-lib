package dev.vality.adapter.flow.lib.flow.full.three.ds.config;

import dev.vality.adapter.common.handler.CommonHandler;
import dev.vality.adapter.common.processor.Processor;
import dev.vality.adapter.flow.lib.client.RemoteClient;
import dev.vality.adapter.flow.lib.converter.base.EntryModelToBaseRequestModelConverter;
import dev.vality.adapter.flow.lib.flow.RecurrentResultIntentResolver;
import dev.vality.adapter.flow.lib.flow.ResultIntentResolver;
import dev.vality.adapter.flow.lib.flow.StepResolver;
import dev.vality.adapter.flow.lib.flow.full.FullThreeDsAllVersionsStepResolverImpl;
import dev.vality.adapter.flow.lib.flow.full.GenerateTokenFullThreeDsAllVersionsStepResolverImpl;
import dev.vality.adapter.flow.lib.flow.full.RecurrentResultIntentResolverImpl;
import dev.vality.adapter.flow.lib.flow.full.ResultIntentResolverImpl;
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
    public ServerFlowHandler serverFlowHandler(
            RemoteClient client,
            EntryModelToBaseRequestModelConverter entryModelToBaseRequestModelConverter,
            Processor<ExitStateModel, BaseResponseModel, EntryStateModel> baseProcessor,
            StepResolver<EntryStateModel, ExitStateModel> fullThreeDsAllVersionsStepResolverImpl) {
        return new ServerFlowHandler(
                getHandlers(client, entryModelToBaseRequestModelConverter, baseProcessor),
                fullThreeDsAllVersionsStepResolverImpl);
    }

    @Bean
    public ServerFlowHandler generateTokenFlowHandler(
            RemoteClient client,
            EntryModelToBaseRequestModelConverter entryModelToBaseRequestModelConverter,
            Processor<ExitStateModel, BaseResponseModel, EntryStateModel> baseProcessor,
            StepResolver<EntryStateModel, ExitStateModel> generateTokenFullThreeDsAllVersionsStepResolverImpl) {
        return new ServerFlowHandler(
                getHandlers(client, entryModelToBaseRequestModelConverter, baseProcessor),
                generateTokenFullThreeDsAllVersionsStepResolverImpl);
    }

    @Bean
    public RecurrentResultIntentResolver recurrentResultIntentResolver(
            TimerProperties timerProperties,
            CallbackUrlExtractor callbackUrlExtractor,
            TagManagementService tagManagementService) {
        return new RecurrentResultIntentResolverImpl(timerProperties, callbackUrlExtractor, tagManagementService);
    }

    @Bean
    public ResultIntentResolver resultIntentResolver(
            TimerProperties timerProperties,
            CallbackUrlExtractor callbackUrlExtractor,
            TagManagementService tagManagementService) {
        return new ResultIntentResolverImpl(timerProperties, callbackUrlExtractor, tagManagementService);
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
