package dev.vality.adapter.flow.lib.utils;

import dev.vality.adapter.common.properties.CommonTimerProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties("time.config")
@Validated
@Getter
@Setter
public class TimerProperties extends CommonTimerProperties {

}
