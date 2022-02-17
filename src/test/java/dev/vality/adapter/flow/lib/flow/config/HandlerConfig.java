package dev.vality.adapter.flow.lib.flow.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.vality.adapter.flow.lib.converter.ExitStateModelToTemporaryContextConverter;
import dev.vality.adapter.flow.lib.converter.base.EntryModelToBaseRequestModelConverter;
import dev.vality.adapter.flow.lib.converter.entry.CtxToEntryModelConverter;
import dev.vality.adapter.flow.lib.converter.entry.RecCtxToEntryModelConverter;
import dev.vality.adapter.flow.lib.converter.exit.ExitModelToProxyResultConverter;
import dev.vality.adapter.flow.lib.converter.exit.ExitModelToRecTokenProxyResultConverter;
import dev.vality.adapter.flow.lib.flow.RecurrentResultIntentResolver;
import dev.vality.adapter.flow.lib.flow.ResultIntentResolver;
import dev.vality.adapter.flow.lib.flow.utils.PaymentContextAdapterConfigurationValidator;
import dev.vality.adapter.flow.lib.flow.utils.RecurrentTokenContextAdapterConfigurationValidator;
import dev.vality.adapter.flow.lib.handler.ProxyProviderServiceImpl;
import dev.vality.adapter.flow.lib.handler.ServerFlowHandler;
import dev.vality.adapter.flow.lib.handler.ServerHandlerLogDecorator;
import dev.vality.adapter.flow.lib.handler.callback.PaymentCallbackHandler;
import dev.vality.adapter.flow.lib.handler.callback.RecurrentTokenCallbackHandler;
import dev.vality.adapter.flow.lib.serde.ParameterSerializer;
import dev.vality.adapter.flow.lib.serde.ParametersDeserializer;
import dev.vality.adapter.flow.lib.serde.TemporaryContextDeserializer;
import dev.vality.adapter.flow.lib.serde.TemporaryContextSerializer;
import dev.vality.adapter.flow.lib.service.*;
import dev.vality.adapter.flow.lib.utils.AdapterProperties;
import dev.vality.adapter.flow.lib.utils.CallbackUrlExtractor;
import dev.vality.adapter.flow.lib.utils.TimerProperties;
import dev.vality.adapter.flow.lib.validator.AdapterConfigurationValidator;
import dev.vality.adapter.helpers.hellgate.HellgateAdapterClient;
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
    public TemporaryContextService temporaryContextService(ParametersDeserializer parametersDeserializer) {
        return new TemporaryContextService(parametersDeserializer);
    }

    @Bean
    public PaymentCallbackHandler paymentCallbackHandler(TemporaryContextDeserializer adapterDeserializer,
                                                         TemporaryContextSerializer temporaryContextSerializer,
                                                         TemporaryContextService temporaryContextService) {
        return new PaymentCallbackHandler(adapterDeserializer,
                temporaryContextSerializer,
                temporaryContextService
        );
    }

    @Bean
    public RecurrentTokenCallbackHandler recurrentTokenCallbackHandler(
            TemporaryContextDeserializer adapterDeserializer,
            TemporaryContextSerializer temporaryContextSerializer,
            TemporaryContextService temporaryContextService) {
        return new RecurrentTokenCallbackHandler(adapterDeserializer,
                temporaryContextSerializer,
                temporaryContextService);
    }

    @Bean
    public CtxToEntryModelConverter ctxToEntryModelConverter(CdsClientStorage cdsClientStorage,
                                                             TemporaryContextDeserializer adapterDeserializer,
                                                             IdGenerator idGenerator,
                                                             TemporaryContextService temporaryContextService,
                                                             CallbackUrlExtractor callbackUrlExtractor) {
        return new CtxToEntryModelConverter(cdsClientStorage,
                adapterDeserializer,
                idGenerator,
                temporaryContextService,
                callbackUrlExtractor);
    }

    @Bean
    public AdapterProperties adapterProperties() {
        AdapterProperties adapterProperties = new AdapterProperties();
        adapterProperties.setCallbackUrl("http://localhost:8080/adapter/term_url");
        adapterProperties.setSuccessRedirectUrl("http://localhost:8080/adapter/term_url");
        return adapterProperties;
    }

    @Bean
    public RecCtxToEntryModelConverter recCtxToEntryModelConverter(CdsClientStorage cdsClientStorage,
                                                                   TemporaryContextDeserializer adapterDeserializer,
                                                                   IdGenerator idGenerator,
                                                                   TemporaryContextService temporaryContextService) {
        return new RecCtxToEntryModelConverter(adapterDeserializer,
                cdsClientStorage,
                idGenerator,
                temporaryContextService);
    }

    @Bean
    public ExitStateModelToTemporaryContextConverter exitStateModelToTemporaryContextConverter() {
        return new ExitStateModelToTemporaryContextConverter();
    }

    @Bean
    public ExitModelToRecTokenProxyResultConverter exitModelToRecTokenProxyResultConverter(
            ErrorMapping errorMapping,
            TemporaryContextSerializer temporaryContextSerializer,
            RecurrentResultIntentResolver recurrentResultIntentResolver,
            ExitStateModelToTemporaryContextConverter exitStateModelToTemporaryContextConverter) {
        return new ExitModelToRecTokenProxyResultConverter(errorMapping,
                temporaryContextSerializer,
                recurrentResultIntentResolver,
                exitStateModelToTemporaryContextConverter
        );
    }

    @Bean
    public ErrorMapping errorMapping() {
        return new ErrorMapping("", List.of());
    }


    @Bean
    public TagManagementService tagManagementService(AdapterProperties adapterProperties) {
        return new TagManagementService(adapterProperties);
    }

    @Bean
    public ParametersDeserializer parametersDeserializer(ObjectMapper objectMapper) {
        return new ParametersDeserializer(objectMapper);
    }

    @Bean
    public ParameterSerializer parameterSerializer(ObjectMapper objectMapper) {
        return new ParameterSerializer(objectMapper);
    }

    @Bean
    public ThreeDsAdapterService threeDsAdapterService(HellgateAdapterClient hgClient,
                                                       ParameterSerializer parameterSerializer,
                                                       ParametersDeserializer parametersDeserializer,
                                                       TagManagementService tagManagementService
    ) {
        return new ThreeDsAdapterService(hgClient, parameterSerializer, parametersDeserializer, tagManagementService);
    }

    @Bean
    public ExitModelToProxyResultConverter exitModelToProxyResultConverter(
            ErrorMapping errorMapping,
            TemporaryContextSerializer temporaryContextSerializer,
            ResultIntentResolver resultIntentResolver,
            ExitStateModelToTemporaryContextConverter exitStateModelToTemporaryContextConverter) {
        return new ExitModelToProxyResultConverter(errorMapping,
                temporaryContextSerializer,
                resultIntentResolver,
                exitStateModelToTemporaryContextConverter);
    }

    @Bean
    public EntryModelToBaseRequestModelConverter entryModelToBaseRequestModelConverter() {
        return new EntryModelToBaseRequestModelConverter();
    }

    @Bean
    public PaymentContextAdapterConfigurationValidator paymentContextValidator() {
        return new PaymentContextAdapterConfigurationValidator();
    }

    @Bean
    public RecurrentTokenContextAdapterConfigurationValidator recurrentTokenContextValidator() {
        return new RecurrentTokenContextAdapterConfigurationValidator();
    }

    @Bean
    public ProviderProxySrv.Iface serverHandlerLogDecorator(
            PaymentCallbackHandler paymentCallbackHandler,
            RecurrentTokenCallbackHandler recurrentTokenCallbackHandler,
            ServerFlowHandler serverFlowHandler,
            ServerFlowHandler generateTokenFlowHandler,
            AdapterConfigurationValidator paymentContextValidator) {
        return new ServerHandlerLogDecorator(new ProxyProviderServiceImpl(
                paymentCallbackHandler,
                recurrentTokenCallbackHandler,
                serverFlowHandler,
                generateTokenFlowHandler,
                paymentContextValidator
        ));
    }

    @Bean
    public IntentResultFactory intentResultFactory(TimerProperties timerProperties,
                                                   CallbackUrlExtractor callbackUrlExtractor,
                                                   TagManagementService tagManagementService) {
        return new IntentResultFactory(timerProperties, callbackUrlExtractor, tagManagementService);
    }


    @Bean
    public RecurrentIntentResultFactory recurrentIntentResultFactory(TimerProperties timerProperties,
                                                                     CallbackUrlExtractor callbackUrlExtractor,
                                                                     TagManagementService tagManagementService) {
        return new RecurrentIntentResultFactory(timerProperties, callbackUrlExtractor, tagManagementService);
    }

}
