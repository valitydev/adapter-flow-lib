package dev.vality.adapter.flow.lib.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ThreeDsV2Callback {

    @JsonProperty("threeDSMethodData")
    private String threeDSMethodData;
    @JsonProperty("threeDSSessionData")
    private String threeDSSessionData;
    @JsonProperty("cres")
    private String cres;

}
