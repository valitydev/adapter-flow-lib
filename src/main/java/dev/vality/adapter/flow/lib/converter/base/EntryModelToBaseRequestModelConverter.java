package dev.vality.adapter.flow.lib.converter.base;

import dev.vality.adapter.flow.lib.model.BaseRequestModel;
import dev.vality.adapter.flow.lib.model.GeneralEntryStateModel;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
public class EntryModelToBaseRequestModelConverter implements Converter<GeneralEntryStateModel, BaseRequestModel> {

    @Override
    public BaseRequestModel convert(GeneralEntryStateModel entryStateModel) {
        return entryStateModel.getBaseRequestModel();
    }

}

