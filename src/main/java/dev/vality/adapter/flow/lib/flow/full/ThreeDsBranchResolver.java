package dev.vality.adapter.flow.lib.flow.full;

import dev.vality.adapter.flow.lib.constant.Status;
import dev.vality.adapter.flow.lib.constant.ThreeDsType;
import dev.vality.adapter.flow.lib.model.ExitStateModel;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ThreeDsBranchResolver {

    public static boolean isRedirectForThreeDsV2Full(ExitStateModel exitStateModel) {
        return isRedirectForThreeDsType(exitStateModel, ThreeDsType.V2_FULL);
    }

    public static boolean isRedirectForThreeDsV2Simple(ExitStateModel exitStateModel) {
        return isRedirectForThreeDsType(exitStateModel, ThreeDsType.V2_SIMPLE);
    }

    public static boolean isRedirectForThreeDsV1(ExitStateModel exitStateModel) {
        return isRedirectForThreeDsType(exitStateModel, ThreeDsType.V1);
    }

    private static boolean isRedirectForThreeDsType(ExitStateModel exitStateModel, ThreeDsType threeDsType) {
        return exitStateModel.getLastOperationStatus() == Status.NEED_REDIRECT
                && exitStateModel.getThreeDsData() != null
                && exitStateModel.getThreeDsData().getThreeDsType() == threeDsType;
    }

}
