package dev.vality.adapter.flow.lib.utils;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Data
@Validated
@ConfigurationProperties("adapter")
public class AdapterProperties {

    @NotEmpty
    private String url;
    private String callbackUrl;
    private String pathCallbackUrl;
    private String pathRecurrentCallbackUrl;

    private String successRedirectUrl;
    private String failedRedirectUrl;

    private String tagPrefix;
    private List<String> tagGeneratorFieldNames = List.of(
            "MD",
            "threeDSMethodData",
            "threeDSSessionData",
            "ThreeDsMethodData",
            "threeDsMethodData",
            "md",
            "ThreeDSMethodData",
            "ThreeDSSessionData",
            "tag"
    );

    private Resource cardHolderNamesFile;

}