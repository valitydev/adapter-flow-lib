package dev.vality.adapter.flow.lib.model;

import dev.vality.adapter.flow.lib.constant.Step;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TemporaryContext {

    private Step nextStep;
    private String providerTrxId;
    private PollingInfo pollingInfo;
    private Map<String, String> threeDsData;
    private byte[] customContext;

}
