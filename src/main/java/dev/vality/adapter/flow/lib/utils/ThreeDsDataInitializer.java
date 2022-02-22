package dev.vality.adapter.flow.lib.utils;

import dev.vality.adapter.flow.lib.model.ExitStateModel;
import dev.vality.adapter.flow.lib.model.ThreeDsData;

import java.util.HashMap;
import java.util.Map;

public class ThreeDsDataInitializer {

    public static final String TAG = "tag";

    public static Map<String, String> initThreeDsParameters(ExitStateModel exitStateModel) {
        Map<String, String> params = new HashMap<>();
        ThreeDsData threeDsData = exitStateModel.getThreeDsData();
        if (threeDsData.getParameters() != null) {
            params.putAll(threeDsData.getParameters());
        } else {
            params.put(TAG, exitStateModel.getProviderTrxId());
        }
        return params;
    }

}
