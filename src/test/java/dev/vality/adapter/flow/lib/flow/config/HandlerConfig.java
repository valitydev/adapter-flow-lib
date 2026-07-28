package dev.vality.adapter.flow.lib.flow.config;

import dev.vality.adapter.common.cds.CdsStorageClient;
import dev.vality.adapter.common.hellgate.HellgateClient;
import dev.vality.adapter.common.v2.mapper.ErrorMapping;
import dev.vality.adapter.flow.lib.converter.ExitStateModelToTemporaryContextConverter;
import dev.vality.adapter.flow.lib.converter.base.EntryModelToBaseRequestModelConverter;
import dev.vality.adapter.flow.lib.converter.entry.CtxToEntryModelConverter;
import dev.vality.adapter.flow.lib.converter.exit.ExitModelToProxyResultConverter;
import dev.vality.adapter.flow.lib.flow.ResultIntentResolver;
import dev.vality.adapter.flow.lib.handler.ProxyProviderServiceImpl;
import dev.vality.adapter.flow.lib.handler.ServerFlowHandler;
import dev.vality.adapter.flow.lib.handler.ServerHandlerLogDecorator;
import dev.vality.adapter.flow.lib.handler.callback.PaymentCallbackHandler;
import dev.vality.adapter.flow.lib.serde.ParametersDeserializer;
import dev.vality.adapter.flow.lib.serde.ParametersSerializer;
import dev.vality.adapter.flow.lib.serde.TemporaryContextDeserializer;
import dev.vality.adapter.flow.lib.serde.TemporaryContextSerializer;
import dev.vality.adapter.flow.lib.service.BenderGenerator;
import dev.vality.adapter.flow.lib.service.CallbackUrlExtractor;
import dev.vality.adapter.flow.lib.service.CardDataServiceWithHolderNamesImpl;
import dev.vality.adapter.flow.lib.service.CardHolderNamesService;
import dev.vality.adapter.flow.lib.service.ExponentialBackOffPollingService;
import dev.vality.adapter.flow.lib.service.IdGenerator;
import dev.vality.adapter.flow.lib.service.PollingInfoService;
import dev.vality.adapter.flow.lib.service.TagManagementService;
import dev.vality.adapter.flow.lib.service.TagManagementServiceImpl;
import dev.vality.adapter.flow.lib.service.TemporaryContextService;
import dev.vality.adapter.flow.lib.service.ThreeDsAdapterService;
import dev.vality.adapter.flow.lib.service.factory.IntentResultFactory;
import dev.vality.adapter.flow.lib.service.factory.SimpleIntentResultFactory;
import dev.vality.adapter.flow.lib.utils.AdapterProperties;
import dev.vality.adapter.flow.lib.utils.TimerProperties;
import dev.vality.adapter.flow.lib.validator.AdapterConfigurationValidator;
import dev.vality.bender.BenderSrv;
import dev.vality.damsel.proxy_provider.PaymentContext;
import dev.vality.damsel.proxy_provider.PaymentProxyResult;
import dev.vality.damsel.proxy_provider.ProviderProxySrv;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.ObjectMapper;

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
    public ExitStateModelToTemporaryContextConverter exitStateModelToTemporaryContextConverter() {
        return new ExitStateModelToTemporaryContextConverter();
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
            ServerFlowHandler<PaymentContext, PaymentProxyResult> serverFlowHandler,
            AdapterConfigurationValidator paymentContextValidator) {
        return new ServerHandlerLogDecorator(new ProxyProviderServiceImpl(
                paymentCallbackHandler,
                serverFlowHandler,
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

}
