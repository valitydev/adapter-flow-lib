package dev.vality.adapter.flow.lib.utils;

import dev.vality.adapter.flow.lib.model.BaseResponseModel;
import org.springframework.util.StringUtils;

public class ErrorUtils {

    public static boolean isError(BaseResponseModel baseResponse) {
        return baseResponse == null || StringUtils.hasText(baseResponse.getErrorCode());
    }

}
