package dev.vality.adapter.flow.lib.flow.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.vality.adapter.flow.lib.serde.ParametersDeserializer;
import dev.vality.adapter.flow.lib.serde.TemporaryContextDeserializer;
import dev.vality.adapter.flow.lib.serde.TemporaryContextSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SerdeConfig {

    @Bean
    public TemporaryContextDeserializer adapterDeserializer(ObjectMapper objectMapper) {
        return new TemporaryContextDeserializer(objectMapper);
    }

    @Bean
    public TemporaryContextSerializer adapterSerializer(ObjectMapper objectMapper) {
        return new TemporaryContextSerializer(objectMapper);
    }

    @Bean
    public ParametersDeserializer parametersDeserializer(ObjectMapper objectMapper) {
        return new ParametersDeserializer(objectMapper);
    }

}
