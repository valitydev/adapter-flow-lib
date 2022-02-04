package dev.vality.adapter.flow.lib.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ThreeDSMethodData {

    private String threeDSMethodNotificationURL;
    private String threeDSServerTransID;

}
