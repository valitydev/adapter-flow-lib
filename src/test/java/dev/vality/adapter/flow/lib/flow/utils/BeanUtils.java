package dev.vality.adapter.flow.lib.flow.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.vality.adapter.common.utils.CommonConverter;
import dev.vality.adapter.flow.lib.constant.Status;
import dev.vality.adapter.flow.lib.constant.ThreeDsType;
import dev.vality.adapter.flow.lib.model.BaseResponseModel;
import dev.vality.adapter.flow.lib.model.QrDisplayData;
import dev.vality.adapter.flow.lib.model.ThreeDsData;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class BeanUtils {

    public static final String TEST_TRX_ID = "testTrxId";
    public static final String PA_RES = "PaRes";
    public static final String MD = "MD";
    public static final String C_RES = "cRes";
    public static final String THREE_DS_SESSION_DATA = "ThreeDSSessionData";
    public static final String THREE_DS_METHOD_DATA = "ThreeDsMethodData";
    public static final String THREE_DS_METHOD_STATE = "ThreeDSMethodState";
    public static final String PA_REQ = "paReq";
    public static final String C_REQ = "cReq";

    public static BaseResponseModel createBaseResponseModel() {
        return BaseResponseModel.builder()
                .providerTrxId(TEST_TRX_ID)
                .status(Status.SUCCESS)
                .build();
    }

    public static ThreeDsData create3Ds1(BaseResponseModel baseResponseModel) {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put(MD, "test_md");
        parameters.put(PA_REQ, "test_pares");
        return ThreeDsData.builder()
                .threeDsType(ThreeDsType.V1)
                .acsUrl("http://localhost/3ds")
                .parameters(parameters)
                .build();
    }

    public static QrDisplayData createQrDisplayData() {
        return QrDisplayData.builder()
                .tagId("testTagId")
                .qrUrl("http://localhost/3ds")
                .build();
    }

    public static ThreeDsData create3Ds2FullCheck() {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put(THREE_DS_METHOD_DATA, "test_threeDsMethodData");
        parameters.put(THREE_DS_METHOD_STATE, "test_threeDSMethodState");
        return ThreeDsData.builder()
                .threeDsType(ThreeDsType.V2_FULL)
                .acsUrl("http://localhost/3ds")
                .parameters(parameters)
                .build();
    }

    public static ThreeDsData create3Ds2FullFinish() {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put(C_REQ, "test_creq");
        parameters.put(THREE_DS_SESSION_DATA, "test_threeDSSessionData");
        return ThreeDsData.builder()
                .threeDsType(ThreeDsType.V2_FULL)
                .acsUrl("http://localhost/3ds")
                .parameters(parameters)
                .build();
    }

    public static ByteBuffer createParesBuffer(String pares, String md) throws JsonProcessingException {
        Map<String, String> map = new HashMap<>();
        map.put(PA_RES, pares);
        map.put(MD, md);
        return CommonConverter.mapToByteBuffer(map);
    }

    public static ByteBuffer createCresBuffer(String cres, String threeDSSessionData) throws JsonProcessingException {
        Map<String, String> map = new HashMap<>();
        map.put(C_RES, cres);
        map.put(THREE_DS_SESSION_DATA, threeDSSessionData);
        return CommonConverter.mapToByteBuffer(map);
    }

    public static ByteBuffer createSessionBuffer(String methodData, String threeDSSessionData)
            throws JsonProcessingException {
        Map<String, String> map = new HashMap<>();
        map.put(THREE_DS_METHOD_DATA, methodData);
        map.put(THREE_DS_METHOD_STATE, threeDSSessionData);
        return CommonConverter.mapToByteBuffer(map);
    }

}
