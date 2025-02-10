package dev.vality.adapter.flow.lib.flow.qr.config;

import dev.vality.adapter.common.mapper.ErrorMapping;
import dev.vality.adapter.flow.lib.client.RemoteClient;
import dev.vality.adapter.flow.lib.converter.base.EntryModelToBaseRequestModelConverter;
import dev.vality.adapter.flow.lib.converter.entry.CtxToEntryModelConverter;
import dev.vality.adapter.flow.lib.converter.exit.ExitModelToProxyResultConverter;
import dev.vality.adapter.flow.lib.flow.RecurrentResultIntentResolver;
import dev.vality.adapter.flow.lib.flow.ResultIntentResolver;
import dev.vality.adapter.flow.lib.flow.StepResolver;
import dev.vality.adapter.flow.lib.flow.simple.SimpleRedirectWithPollingResultIntentResolver;
import dev.vality.adapter.flow.lib.flow.simple.SimpleRedirectWithPollingStepResolverImpl;
import dev.vality.adapter.flow.lib.flow.simple.UnsupportedGenerateTokenResultIntentResolver;
import dev.vality.adapter.flow.lib.flow.simple.UnsupportedGenerateTokenStepResolverImpl;
import dev.vality.adapter.flow.lib.handler.CommonHandler;
import dev.vality.adapter.flow.lib.handler.ServerFlowHandler;
import dev.vality.adapter.flow.lib.handler.ServerFlowHandlerImpl;
import dev.vality.adapter.flow.lib.handler.payment.*;
import dev.vality.adapter.flow.lib.model.BaseResponseModel;
import dev.vality.adapter.flow.lib.model.EntryStateModel;
import dev.vality.adapter.flow.lib.model.ExitStateModel;
import dev.vality.adapter.flow.lib.processor.*;
import dev.vality.adapter.flow.lib.serde.ParametersSerializer;
import dev.vality.adapter.flow.lib.service.ExponentialBackOffPollingService;
import dev.vality.adapter.flow.lib.service.PollingInfoService;
import dev.vality.adapter.flow.lib.service.TagManagementService;
import dev.vality.adapter.flow.lib.service.factory.IntentResultQrPaymentFactory;
import dev.vality.adapter.flow.lib.utils.TimerProperties;
import dev.vality.damsel.proxy_provider.PaymentContext;
import dev.vality.damsel.proxy_provider.PaymentProxyResult;
import dev.vality.damsel.proxy_provider.RecurrentTokenContext;
import dev.vality.damsel.proxy_provider.RecurrentTokenProxyResult;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

@Configuration
public class QrRedirectWithPollingDsFlowConfig {

    @Bean
    public StepResolver<EntryStateModel, ExitStateModel> generateTokenStepResolverImpl() {
        return new UnsupportedGenerateTokenStepResolverImpl();
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
    @Primary
    public Processor<ExitStateModel, BaseResponseModel, EntryStateModel> baseProcessor() {
        ErrorProcessor errorProcessor = new ErrorProcessor();
        SuccessFinishProcessor baseProcessor = new SuccessFinishProcessor(errorProcessor);
        QrDisplayProcessor qrDisplayProcessor = new QrDisplayProcessor(baseProcessor);
        return new RetryProcessor(qrDisplayProcessor);
    }

    @Bean
    public RecurrentResultIntentResolver recurrentResultIntentResolver() {
        return new UnsupportedGenerateTokenResultIntentResolver();
    }

    @Bean
    public ResultIntentResolver resultIntentResolver(IntentResultQrPaymentFactory intentResultFactory) {
        return new SimpleRedirectWithPollingResultIntentResolver(intentResultFactory);
    }

    @Bean
    public ServerFlowHandler<RecurrentTokenContext, RecurrentTokenProxyResult> generateTokenFlowHandler() {
        return new ServerFlowHandler<>() {
        };
    }

    @Bean
    public IntentResultQrPaymentFactory intentResultFactory(
            TimerProperties timerProperties,
            TagManagementService tagManagementService,
            ParametersSerializer parametersSerializer,
            PollingInfoService pollingInfoService,
            ErrorMapping errorMapping,
            ExponentialBackOffPollingService exponentialBackOffPollingService) {
        return new IntentResultQrPaymentFactory(timerProperties, tagManagementService,
                parametersSerializer, pollingInfoService, errorMapping, exponentialBackOffPollingService);
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
