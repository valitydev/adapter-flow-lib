package dev.vality.adapter.flow.lib.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AdditionalTrxInfo {

    private String rrn;
    private String approvalCode;
    private Map<String, String> extraInfo;

}
