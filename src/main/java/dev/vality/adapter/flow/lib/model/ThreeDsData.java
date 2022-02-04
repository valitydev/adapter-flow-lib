package dev.vality.adapter.flow.lib.model;

import dev.vality.adapter.flow.lib.constant.ThreeDsType;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Data
@SuperBuilder
public class ThreeDsData {

    private String uniqRedirectOperationIdName;
    private ThreeDsType threeDsType;
    private String acsUrl;
    private Map<String, String> parameters;

}
