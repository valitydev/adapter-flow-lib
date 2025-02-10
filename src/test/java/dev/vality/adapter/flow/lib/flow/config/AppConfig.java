package dev.vality.adapter.flow.lib.flow.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.vality.adapter.common.component.SimpleErrorMapping;
import dev.vality.adapter.common.mapper.ErrorMapping;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
public class AppConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() {
        PropertySourcesPlaceholderConfigurer pspc
                = new PropertySourcesPlaceholderConfigurer();
        Resource[] resources = new ClassPathResource[]
                {new ClassPathResource("application.yaml")};
        pspc.setLocations(resources);
        pspc.setIgnoreUnresolvablePlaceholders(true);
        return pspc;
    }

    @Bean
    public ErrorMapping errorMapping(@Value("${error-mapping.file}") Resource errorMappingFilePath,
                                     @Value("${error-mapping.patternReason:\"'%s' - '%s'\"}")
                                             String errorMappingPattern) throws IOException {
        return new SimpleErrorMapping(errorMappingFilePath, errorMappingPattern).createErrorMapping();
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
