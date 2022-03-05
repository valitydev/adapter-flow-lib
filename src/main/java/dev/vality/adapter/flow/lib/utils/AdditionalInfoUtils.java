package dev.vality.adapter.flow.lib.utils;

import dev.vality.adapter.flow.lib.model.ExitStateModel;
import dev.vality.damsel.domain.AdditionalTransactionInfo;

public class AdditionalInfoUtils {

    public static AdditionalTransactionInfo initAdditionalTrxInfo(ExitStateModel exitStateModel) {
        if (exitStateModel.getAdditionalTrxInfo() != null) {
            return new AdditionalTransactionInfo()
                    .setRrn(exitStateModel.getAdditionalTrxInfo().getRrn())
                    .setApprovalCode(exitStateModel.getAdditionalTrxInfo().getApprovalCode());
        }
        return null;
    }

}
