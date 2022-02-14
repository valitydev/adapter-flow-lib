package dev.vality.adapter.flow.lib.flow.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.vality.adapter.common.handler.ServerHandlerLogDecorator;
import dev.vality.adapter.flow.lib.converter.ExitStateModelToTemporaryContextConverter;
import dev.vality.adapter.flow.lib.converter.base.EntryModelToBaseRequestModelConverter;
import dev.vality.adapter.flow.lib.converter.entry.CtxToEntryModelConverter;
import dev.vality.adapter.flow.lib.converter.entry.RecCtxToEntryModelConverter;
import dev.vality.adapter.flow.lib.converter.exit.ExitModelToProxyResultConverter;
import dev.vality.adapter.flow.lib.converter.exit.ExitModelToRecTokenProxyResultConverter;
import dev.vality.adapter.flow.lib.flow.RecurrentResultIntentResolver;
import dev.vality.adapter.flow.lib.flow.ResultIntentResolver;
import dev.vality.adapter.flow.lib.flow.utils.PaymentContextValidator;
import dev.vality.adapter.flow.lib.flow.utils.RecurrentTokenContextValidator;
import dev.vality.adapter.flow.lib.handler.AdapterServerHandler;
import dev.vality.adapter.flow.lib.handler.ServerFlowHandler;
import dev.vality.adapter.flow.lib.handler.callback.PaymentCallbackHandler;
import dev.vality.adapter.flow.lib.handler.callback.RecurrentTokenCallbackHandler;
import dev.vality.adapter.flow.lib.service.IdGenerator;
import dev.vality.adapter.flow.lib.service.TagManagementService;
import dev.vality.adapter.flow.lib.service.ThreeDsAdapterService;
import dev.vality.adapter.flow.lib.utils.*;
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
    public PaymentCallbackHandler paymentCallbackHandler(TemporaryContextDeserializer adapterDeserializer,
                                                         TemporaryContextSerializer temporaryContextSerializer,
                                                         ParametersDeserializer threeDsV2CallbackDeserializer) {
        return new PaymentCallbackHandler(adapterDeserializer,
                temporaryContextSerializer,
                threeDsV2CallbackDeserializer);
    }

    @Bean
    public RecurrentTokenCallbackHandler recurrentTokenCallbackHandler(
            TemporaryContextDeserializer adapterDeserializer,
            TemporaryContextSerializer temporaryContextSerializer,
            ParametersDeserializer parametersDeserializer) {
        return new RecurrentTokenCallbackHandler(adapterDeserializer,
                temporaryContextSerializer,
                parametersDeserializer);
    }

    @Bean
    public CtxToEntryModelConverter ctxToEntryModelConverter(CdsClientStorage cdsClientStorage,
                                                             TemporaryContextDeserializer adapterDeserializer,
                                                             IdGenerator idGenerator) {
        return new CtxToEntryModelConverter(cdsClientStorage,
                adapterDeserializer,
                idGenerator);
    }

    @Bean
    public AdapterProperties adapterProperties() {
        AdapterProperties adapterProperties = new AdapterProperties();
        adapterProperties.setCallbackUrl("http://localhost:8080/adapter/term_url");
        adapterProperties.setDefaultTermUrl("http://localhost:8080/adapter/term_url");
        return adapterProperties;
    }

    @Bean
    public RecCtxToEntryModelConverter recCtxToEntryModelConverter(CdsClientStorage cdsClientStorage,
                                                                   TemporaryContextDeserializer adapterDeserializer,
                                                                   IdGenerator idGenerator) {
        return new RecCtxToEntryModelConverter(adapterDeserializer,
                cdsClientStorage,
                idGenerator);
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
    public PaymentContextValidator paymentContextValidator() {
        return new PaymentContextValidator();
    }

    @Bean
    public RecurrentTokenContextValidator recurrentTokenContextValidator() {
        return new RecurrentTokenContextValidator();
    }

    @Bean
    public ProviderProxySrv.Iface serverHandlerLogDecorator(
            PaymentContextValidator paymentContextValidator,
            RecurrentTokenContextValidator recurrentTokenContextValidator,
            PaymentCallbackHandler paymentCallbackHandler,
            RecurrentTokenCallbackHandler recurrentTokenCallbackHandler,
            CtxToEntryModelConverter ctxToEntryModelConverter,
            RecCtxToEntryModelConverter recCtxToEntryStateModelConverter,
            ExitModelToRecTokenProxyResultConverter exitModelToRecTokenProxyResultConverter,
            ExitModelToProxyResultConverter exitModelToProxyResultConverter,
            ServerFlowHandler serverFlowHandler,
            ServerFlowHandler generateTokenFlowHandler) {
        return new ServerHandlerLogDecorator(new AdapterServerHandler(paymentContextValidator,
                recurrentTokenContextValidator,
                paymentCallbackHandler,
                recurrentTokenCallbackHandler,
                ctxToEntryModelConverter,
                recCtxToEntryStateModelConverter,
                exitModelToRecTokenProxyResultConverter,
                exitModelToProxyResultConverter,
                serverFlowHandler,
                generateTokenFlowHandler
        ));
    }

}
