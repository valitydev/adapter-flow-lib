package dev.vality.adapter.flow.lib.flow.utils;

import dev.vality.adapter.flow.lib.constant.ThreeDsType;
import dev.vality.adapter.flow.lib.model.BaseResponseModel;
import dev.vality.adapter.flow.lib.model.ThreeDsData;

import java.util.HashMap;

public class BeanUtils {

    public static final String TEST_TRX_ID = "testTrxId";

    public static BaseResponseModel createBaseResponseModel() {
        return BaseResponseModel.builder()
                .providerTrxId(TEST_TRX_ID)
                .build();
    }

    public static ThreeDsData create3Ds1(BaseResponseModel baseResponseModel) {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("MD", "test_md");
        parameters.put("paReq", "test_pares");
        return ThreeDsData.builder()
                .threeDsType(ThreeDsType.V1)
                .acsUrl("http://localhost/3ds")
                .parameters(parameters)
                .build();
    }

    public static ThreeDsData create3Ds2Full(BaseResponseModel baseResponseModel) {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("ThreeDsMethodData", "test_threeDsMethodData");
        parameters.put("ThreeDSMethodState", "test_threeDSMethodState");
        return ThreeDsData.builder()
                .threeDsType(ThreeDsType.V2_FULL)
                .acsUrl("http://localhost/3ds")
                .parameters(parameters)
                .build();
    }
}
