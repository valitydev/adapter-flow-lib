package dev.vality.adapter.flow.lib.flow.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.vality.adapter.common.cds.CdsStorageClient;
import dev.vality.adapter.common.hellgate.HellgateClient;
import dev.vality.adapter.common.mapper.ErrorMapping;
import dev.vality.adapter.flow.lib.converter.ExitStateModelToTemporaryContextConverter;
import dev.vality.adapter.flow.lib.converter.base.EntryModelToBaseRequestModelConverter;
import dev.vality.adapter.flow.lib.converter.entry.CtxToEntryModelConverter;
import dev.vality.adapter.flow.lib.converter.entry.RecCtxToEntryModelConverter;
import dev.vality.adapter.flow.lib.converter.exit.ExitModelToProxyResultConverter;
import dev.vality.adapter.flow.lib.converter.exit.ExitModelToRecTokenProxyResultConverter;
import dev.vality.adapter.flow.lib.flow.RecurrentResultIntentResolver;
import dev.vality.adapter.flow.lib.flow.ResultIntentResolver;
import dev.vality.adapter.flow.lib.handler.ProxyProviderServiceImpl;
import dev.vality.adapter.flow.lib.handler.ServerFlowHandler;
import dev.vality.adapter.flow.lib.handler.ServerHandlerLogDecorator;
import dev.vality.adapter.flow.lib.handler.callback.PaymentCallbackHandler;
import dev.vality.adapter.flow.lib.handler.callback.RecurrentTokenCallbackHandler;
import dev.vality.adapter.flow.lib.serde.ParametersDeserializer;
import dev.vality.adapter.flow.lib.serde.ParametersSerializer;
import dev.vality.adapter.flow.lib.serde.TemporaryContextDeserializer;
import dev.vality.adapter.flow.lib.serde.TemporaryContextSerializer;
import dev.vality.adapter.flow.lib.service.*;
import dev.vality.adapter.flow.lib.service.factory.IntentResultFactory;
import dev.vality.adapter.flow.lib.service.factory.SimpleIntentResultFactory;
import dev.vality.adapter.flow.lib.service.factory.SimpleRecurrentIntentResultFactory;
import dev.vality.adapter.flow.lib.utils.AdapterProperties;
import dev.vality.adapter.flow.lib.service.CallbackUrlExtractor;
import dev.vality.adapter.flow.lib.utils.TimerProperties;
import dev.vality.adapter.flow.lib.validator.AdapterConfigurationValidator;
import dev.vality.bender.BenderSrv;
import dev.vality.damsel.proxy_provider.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class HandlerConfig {

    @Bean
    public IdGenerator idGenerator(BenderSrv.Iface iface) {
        return new BenderGenerator(iface);
    }

    @Bean
    public TimerProperties timerProperties() {
        TimerProperties timerProperties = new TimerProperties();
        timerProperties.setMaxTimePollingMin(60);
        timerProperties.setPollingDelayMs(1000);
        timerProperties.setRedirectTimeoutMin(15);
        return timerProperties;
    }

    @Bean
    public PollingInfoService pollingInfoService(TimerProperties timerProperties) {
        return new PollingInfoService(timerProperties);
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
    public CardHolderNamesService cardHolderNamesService(AdapterProperties properties) throws IOException {
        return new CardHolderNamesService(properties);
    }

    @Bean
    public CardDataServiceWithHolderNamesImpl cardDataService(CdsStorageClient cdsStorageClient,
                                                              CardHolderNamesService cardHolderNamesService) {
        return new CardDataServiceWithHolderNamesImpl(cdsStorageClient, cardHolderNamesService);
    }

    @Bean
    public CtxToEntryModelConverter ctxToEntryModelConverter(CdsStorageClient cdsStorageClient,
                                                             TemporaryContextDeserializer adapterDeserializer,
                                                             IdGenerator idGenerator,
                                                             TemporaryContextService temporaryContextService,
                                                             CallbackUrlExtractor callbackUrlExtractor,
                                                             CardDataServiceWithHolderNamesImpl cardDataService,
                                                             AdapterProperties adapterProperties) {
        return new CtxToEntryModelConverter(cdsStorageClient,
                adapterDeserializer,
                idGenerator,
                temporaryContextService,
                callbackUrlExtractor,
                cardDataService,
                adapterProperties);
    }

    @Bean
    public AdapterProperties adapterProperties() {
        AdapterProperties adapterProperties = new AdapterProperties();
        adapterProperties.setCallbackUrl("http://localhost:8080/adapter/term_url");
        adapterProperties.setSuccessRedirectUrl("http://localhost:8080/adapter/term_url");
        adapterProperties.setCardHolderNamesFile(new ClassPathResource("csv/holders.csv"));
        return adapterProperties;
    }

    @Bean
    public RecCtxToEntryModelConverter recCtxToEntryModelConverter(CdsStorageClient cdsStorageClient,
                                                                   TemporaryContextDeserializer adapterDeserializer,
                                                                   IdGenerator idGenerator,
                                                                   TemporaryContextService temporaryContextService,
                                                                   CardDataServiceWithHolderNamesImpl cardDataService) {
        return new RecCtxToEntryModelConverter(adapterDeserializer,
                cdsStorageClient,
                idGenerator,
                temporaryContextService,
                cardDataService);
    }

    @Bean
    public ExitStateModelToTemporaryContextConverter exitStateModelToTemporaryContextConverter() {
        return new ExitStateModelToTemporaryContextConverter();
    }

    @Bean
    public ExitModelToRecTokenProxyResultConverter exitModelToRecTokenProxyResultConverter(
            SimpleRecurrentIntentResultFactory recurrentIntentResultFactory,
            TemporaryContextSerializer temporaryContextSerializer,
            RecurrentResultIntentResolver recurrentResultIntentResolver,
            ExitStateModelToTemporaryContextConverter exitStateModelToTemporaryContextConverter) {
        return new ExitModelToRecTokenProxyResultConverter(recurrentIntentResultFactory,
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
        return new TagManagementServiceImpl(adapterProperties);
    }

    @Bean
    public ParametersDeserializer parametersDeserializer(ObjectMapper objectMapper) {
        return new ParametersDeserializer(objectMapper);
    }

    @Bean
    public ParametersSerializer parameterSerializer(ObjectMapper objectMapper) {
        return new ParametersSerializer(objectMapper);
    }

    @Bean
    public ThreeDsAdapterService threeDsAdapterService(HellgateClient hellgateClient,
                                                       ParametersSerializer parametersSerializer,
                                                       ParametersDeserializer parametersDeserializer,
                                                       TagManagementService tagManagementService
    ) {
        return new ThreeDsAdapterService(
                hellgateClient, parametersSerializer, parametersDeserializer, tagManagementService);
    }

    @Bean
    public ExitModelToProxyResultConverter exitModelToProxyResultConverter(
            IntentResultFactory intentResultFactory,
            TemporaryContextSerializer temporaryContextSerializer,
            ResultIntentResolver resultIntentResolver,
            ExitStateModelToTemporaryContextConverter exitStateModelToTemporaryContextConverter) {
        return new ExitModelToProxyResultConverter(intentResultFactory,
                temporaryContextSerializer,
                resultIntentResolver,
                exitStateModelToTemporaryContextConverter);
    }

    @Bean
    public EntryModelToBaseRequestModelConverter entryModelToBaseRequestModelConverter() {
        return new EntryModelToBaseRequestModelConverter();
    }

    @Bean
    public ProviderProxySrv.Iface serverHandlerLogDecorator(
            PaymentCallbackHandler paymentCallbackHandler,
            RecurrentTokenCallbackHandler recurrentTokenCallbackHandler,
            ServerFlowHandler<PaymentContext, PaymentProxyResult> serverFlowHandler,
            ServerFlowHandler<RecurrentTokenContext, RecurrentTokenProxyResult> generateTokenFlowHandler,
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
    public ExponentialBackOffPollingService exponentialBackOffPollingService() {
        return new ExponentialBackOffPollingService();
    }

    @Bean
    public SimpleIntentResultFactory intentResultFactory(
            TimerProperties timerProperties,
            CallbackUrlExtractor callbackUrlExtractor,
            TagManagementService tagManagementService,
            ParametersSerializer parametersSerializer,
            PollingInfoService pollingInfoService,
            ErrorMapping errorMapping,
            ExponentialBackOffPollingService exponentialBackOffPollingService) {
        return new SimpleIntentResultFactory(timerProperties, callbackUrlExtractor, tagManagementService,
                parametersSerializer, pollingInfoService, errorMapping, exponentialBackOffPollingService);
    }

    @Bean
    public SimpleRecurrentIntentResultFactory recurrentIntentResultFactory(
            TimerProperties timerProperties,
            CallbackUrlExtractor callbackUrlExtractor,
            TagManagementService tagManagementService,
            PollingInfoService pollingInfoService,
            ErrorMapping errorMapping,
            ExponentialBackOffPollingService exponentialBackOffPollingService) {
        return new SimpleRecurrentIntentResultFactory(timerProperties, callbackUrlExtractor, tagManagementService,
                pollingInfoService, errorMapping, exponentialBackOffPollingService);
    }

}
