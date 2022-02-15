package dev.vality.adapter.flow.lib.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.vality.adapter.common.state.serializer.StateSerializer;

import java.util.Map;

public class ParameterSerializer extends StateSerializer<Map<String, String>> {
    public ParameterSerializer(ObjectMapper mapper) {
        super(mapper);
    }
}