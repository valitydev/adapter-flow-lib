package dev.vality.adapter.flow.lib.serde;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class ParameterSerializer extends StateSerializer<Map<String, String>> {
    public ParameterSerializer(ObjectMapper mapper) {
        super(mapper);
    }
}