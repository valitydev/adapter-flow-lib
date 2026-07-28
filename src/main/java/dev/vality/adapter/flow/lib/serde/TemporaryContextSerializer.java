package dev.vality.adapter.flow.lib.serde;

import dev.vality.adapter.flow.lib.model.TemporaryContext;
import tools.jackson.databind.ObjectMapper;

public class TemporaryContextSerializer extends StateSerializer<TemporaryContext> {
    public TemporaryContextSerializer(ObjectMapper mapper) {
        super(mapper);
    }
}
