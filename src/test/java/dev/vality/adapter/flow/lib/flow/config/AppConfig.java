package dev.vality.adapter.flow.lib.flow.config;

import dev.vality.adapter.common.component.SimpleErrorMapping;
import dev.vality.adapter.common.v2.mapper.ErrorMapping;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

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
                                     String errorMappingPattern)
            throws IOException {
        return new SimpleErrorMapping(errorMappingFilePath, errorMappingPattern)
                .createErrorMapping();
    }

    @Bean
    public JsonMapper objectMapper() {
        return JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
    }
}
