package dev.vality.adapter.flow.lib.serde;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class ParametersSerializer extends StateSerializer<Map<String, String>> {
    public ParametersSerializer(ObjectMapper mapper) {
        super(mapper);
    }
}