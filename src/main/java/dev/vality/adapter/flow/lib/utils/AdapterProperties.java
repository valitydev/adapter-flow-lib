package dev.vality.adapter.flow.lib.utils;

import dev.vality.adapter.common.properties.CommonAdapterProperties;
import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Data
@Configuration
@ConfigurationProperties("adapter")
@Validated
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AdapterProperties extends CommonAdapterProperties {

    private String defaultTermUrl;

}