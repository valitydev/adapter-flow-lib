package dev.vality.adapter.flow.lib.model;

import dev.vality.adapter.flow.lib.constant.Status;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Data
@SuperBuilder
public class BaseResponseModel {

    private Status status;

    private String errorCode;
    private String errorMessage;

    private String providerTrxId;
    private String recurrentToken;
    private Map<String, String> saveData;

    private ThreeDsData threeDsData;

}