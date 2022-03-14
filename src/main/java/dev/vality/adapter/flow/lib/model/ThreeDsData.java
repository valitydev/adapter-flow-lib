package dev.vality.adapter.flow.lib.model;

import dev.vality.adapter.flow.lib.constant.HttpMethod;
import dev.vality.adapter.flow.lib.constant.ThreeDsType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ThreeDsData {

    /**
     * Http method type.
     */
    private HttpMethod httpMethod = HttpMethod.GET;
    /**
     * Use type to select desired 3ds flow.
     */
    private ThreeDsType threeDsType;
    /**
     * Url for redirect user.
     */
    private String acsUrl;
    /**
     * Parameters that would send in redirect request.
     * Name and value of parameters don't change and forward send in your format.
     */
    private Map<String, String> parameters;

}
