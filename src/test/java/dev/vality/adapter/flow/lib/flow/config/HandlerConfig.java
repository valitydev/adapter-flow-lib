package dev.vality.adapter.flow.lib.flow.config;

import dev.vality.adapter.common.handler.ServerHandlerLogDecorator;
import dev.vality.adapter.common.processor.Processor;
import dev.vality.adapter.common.state.deserializer.AdapterDeserializer;
import dev.vality.adapter.common.state.deserializer.CallbackDeserializer;
import dev.vality.adapter.common.state.serializer.AdapterSerializer;
import dev.vality.adapter.flow.lib.client.RemoteClient;
import dev.vality.adapter.flow.lib.converter.base.EntryModelToBaseRequestModelConverter;
import dev.vality.adapter.flow.lib.converter.entry.CtxToEntryModelConverter;
import dev.vality.adapter.flow.lib.converter.entry.RecCtxToEntryModelConverter;
import dev.vality.adapter.flow.lib.converter.exit.ExitModelToProxyResultConverter;
import dev.vality.adapter.flow.lib.converter.exit.ExitModelToRecTokenProxyResultConverter;
import dev.vality.adapter.flow.lib.flow.StepResolver;
import dev.vality.adapter.flow.lib.flow.utils.PaymentContextValidator;
import dev.vality.adapter.flow.lib.flow.utils.RecurrentTokenContextValidator;
import dev.vality.adapter.flow.lib.handler.AdapterServerHandler;
import dev.vality.adapter.flow.lib.handler.ServerFlowHandler;
import dev.vality.adapter.flow.lib.handler.callback.PaymentCallbackHandler;
import dev.vality.adapter.flow.lib.handler.callback.RecurrentTokenCallbackHandler;
import dev.vality.adapter.flow.lib.handler.payment.*;
import dev.vality.adapter.flow.lib.model.BaseResponseModel;
import dev.vality.adapter.flow.lib.model.GeneralEntryStateModel;
import dev.vality.adapter.flow.lib.model.GeneralExitStateModel;
import dev.vality.adapter.flow.lib.service.IdGenerator;
import dev.vality.adapter.flow.lib.service.ResultIntentResolver;
import dev.vality.adapter.flow.lib.utils.AdapterProperties;
import dev.vality.adapter.flow.lib.utils.CallbackUrlExtractor;
import dev.vality.adapter.flow.lib.utils.ThreeDsV2CallbackDeserializer;
import dev.vality.adapter.flow.lib.utils.TimerProperties;
import dev.vality.bender.BenderSrv;
import dev.vality.cds.client.storage.CdsClientStorage;
import dev.vality.damsel.proxy_provider.ProviderProxySrv;
import dev.vality.error.mapping.ErrorMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class HandlerConfig {

    @Bean
    public IdGenerator idGenerator(BenderSrv.Iface iface) {
        return new IdGenerator(iface);
    }

    @Bean
    public PaymentCallbackHandler paymentCallbackHandler(AdapterDeserializer adapterDeserializer,
                                                         AdapterSerializer adapterSerializer,
                                                         CallbackDeserializer callbackDeserializer,
                                                         ThreeDsV2CallbackDeserializer threeDsV2CallbackDeserializer) {
        return new PaymentCallbackHandler(adapterDeserializer,
                adapterSerializer,
                callbackDeserializer,
                threeDsV2CallbackDeserializer);
    }

    @Bean
    public RecurrentTokenCallbackHandler recurrentTokenCallbackHandler(
            AdapterDeserializer adapterDeserializer,
            AdapterSerializer adapterSerializer,
            CallbackDeserializer callbackDeserializer,
            ThreeDsV2CallbackDeserializer threeDsV2CallbackDeserializer) {
        return new RecurrentTokenCallbackHandler(adapterDeserializer,
                adapterSerializer,
                callbackDeserializer,
                threeDsV2CallbackDeserializer);
    }

    @Bean
    public CtxToEntryModelConverter ctxToEntryModelConverter(CdsClientStorage cdsClientStorage,
                                                             AdapterDeserializer adapterDeserializer,
                                                             IdGenerator idGenerator) {
        return new CtxToEntryModelConverter(cdsClientStorage,
                adapterDeserializer,
                idGenerator);
    }


    @Bean
    public RecCtxToEntryModelConverter recCtxToEntryModelConverter(CdsClientStorage cdsClientStorage,
                                                                   AdapterDeserializer adapterDeserializer,
                                                                   IdGenerator idGenerator,
                                                                   AdapterProperties adapterProperties) {
        return new RecCtxToEntryModelConverter(adapterDeserializer,
                cdsClientStorage,
                adapterProperties,
                idGenerator);
    }

    @Bean
    public ExitModelToRecTokenProxyResultConverter exitModelToRecTokenProxyResultConverter() {
        return new ExitModelToRecTokenProxyResultConverter(null, null, null);
    }

    @Bean
    public ErrorMapping errorMapping() {
        return new ErrorMapping("", List.of());
    }

    @Bean
    public ResultIntentResolver resultIntentResolver(
            TimerProperties timerProperties,
            CallbackUrlExtractor callbackUrlExtractor) {
        return new ResultIntentResolver(timerProperties, callbackUrlExtractor);
    }

    @Bean
    public ExitModelToProxyResultConverter exitModelToProxyResultConverter(
            ErrorMapping errorMapping,
            AdapterSerializer adapterSerializer,
            ResultIntentResolver resultIntentResolver) {
        return new ExitModelToProxyResultConverter(errorMapping,
                adapterSerializer,
                resultIntentResolver);
    }

    @Bean
    public EntryModelToBaseRequestModelConverter entryModelToBaseRequestModelConverter() {
        return new EntryModelToBaseRequestModelConverter();
    }

    @Bean
    public ServerFlowHandler serverFlowHandler(RemoteClient client,
                                               EntryModelToBaseRequestModelConverter entryModelToBaseRequestModelConverter,
                                               Processor<GeneralExitStateModel, BaseResponseModel, GeneralEntryStateModel> baseProcessor,
                                               StepResolver<GeneralEntryStateModel, GeneralExitStateModel> stepResolverImpl) {
        return new ServerFlowHandler(
                List.of(new AuthHandler(client, entryModelToBaseRequestModelConverter, baseProcessor),
                        new CancelHandler(client, entryModelToBaseRequestModelConverter, baseProcessor),
                        new CaptureHandler(client, entryModelToBaseRequestModelConverter, baseProcessor),
                        new Check3dsV2Handler(client, entryModelToBaseRequestModelConverter, baseProcessor),
                        new DoNothingHandler(),
                        new Finish3dsHandler(client, entryModelToBaseRequestModelConverter, baseProcessor),
                        new Finish3dsV2Handler(client, entryModelToBaseRequestModelConverter, baseProcessor),
                        new PaymentHandler(client, entryModelToBaseRequestModelConverter, baseProcessor),
                        new RefundHandler(client, entryModelToBaseRequestModelConverter, baseProcessor)),
                stepResolverImpl);
    }

    @Bean
    public PaymentContextValidator paymentContextValidator() {
        return new PaymentContextValidator();
    }

    @Bean
    public RecurrentTokenContextValidator recurrentTokenContextValidator() {
        return new RecurrentTokenContextValidator();
    }

    @Bean
    public ProviderProxySrv.Iface adapterServerHandler(
            PaymentContextValidator paymentContextValidator,
            RecurrentTokenContextValidator recurrentTokenContextValidator,
            PaymentCallbackHandler paymentCallbackHandler,
            RecurrentTokenCallbackHandler recurrentTokenCallbackHandler,
            CtxToEntryModelConverter ctxToEntryModelConverter,
            RecCtxToEntryModelConverter recCtxToEntryStateModelConverter,
            ExitModelToRecTokenProxyResultConverter exitModelToRecTokenProxyResultConverter,
            ExitModelToProxyResultConverter exitModelToProxyResultConverter,
            ServerFlowHandler serverFlowHandler) {
        return new AdapterServerHandler(paymentContextValidator,
                recurrentTokenContextValidator,
                paymentCallbackHandler,
                recurrentTokenCallbackHandler,
                ctxToEntryModelConverter,
                recCtxToEntryStateModelConverter,
                exitModelToRecTokenProxyResultConverter,
                exitModelToProxyResultConverter,
                serverFlowHandler);
    }

    @Bean
    public ProviderProxySrv.Iface serverHandlerLogDecorator(ProviderProxySrv.Iface adapterServerHandler) {
        return new ServerHandlerLogDecorator(adapterServerHandler);
    }

}
