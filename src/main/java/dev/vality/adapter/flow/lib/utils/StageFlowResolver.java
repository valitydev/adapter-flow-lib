package dev.vality.adapter.flow.lib.utils;

import dev.vality.adapter.flow.lib.constant.OptionFields;
import dev.vality.adapter.flow.lib.constant.Stage;
import dev.vality.adapter.flow.lib.model.EntryStateModel;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StageFlowResolver {

    public static boolean isOneStageFlow(EntryStateModel stateModel) {
        return Stage.ONE.equals(stateModel.getBaseRequestModel().getAdapterConfigurations()
                .get(OptionFields.STAGE.name()));
    }

}
