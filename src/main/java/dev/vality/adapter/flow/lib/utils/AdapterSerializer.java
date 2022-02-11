package dev.vality.adapter.flow.lib.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.vality.adapter.common.state.serializer.StateSerializer;
import dev.vality.adapter.flow.lib.model.TemporaryContext;

public class AdapterSerializer extends StateSerializer<TemporaryContext> {
    public AdapterSerializer(ObjectMapper mapper) {
        super(mapper);
    }
}