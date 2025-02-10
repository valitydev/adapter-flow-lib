package dev.vality.adapter.flow.lib.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.vality.adapter.flow.lib.model.TemporaryContext;

public class TemporaryContextSerializer extends StateSerializer<TemporaryContext> {
    public TemporaryContextSerializer(ObjectMapper mapper) {
        super(mapper);
    }
}