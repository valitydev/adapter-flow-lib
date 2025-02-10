package dev.vality.adapter.flow.lib.converter.base;

import dev.vality.adapter.flow.lib.model.BaseRequestModel;
import dev.vality.adapter.flow.lib.model.EntryStateModel;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;

@RequiredArgsConstructor
public class EntryModelToBaseRequestModelConverter implements Converter<EntryStateModel, BaseRequestModel> {

    @Override
    public BaseRequestModel convert(EntryStateModel entryStateModel) {
        return entryStateModel.getBaseRequestModel();
    }

}

