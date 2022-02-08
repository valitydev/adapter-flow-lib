package dev.vality.adapter.flow.lib.model;

import dev.vality.adapter.flow.lib.constant.ThreeDsType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ThreeDsData {

    private ThreeDsType threeDsType;
    private String acsUrl;
    private Map<String, String> parameters;

}
