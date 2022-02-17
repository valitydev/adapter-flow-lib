package dev.vality.adapter.flow.lib.utils;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@Validated
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
            "ThreeDSSessionData"
    );

}