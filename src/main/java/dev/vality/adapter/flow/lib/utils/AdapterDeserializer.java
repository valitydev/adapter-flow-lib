package dev.vality.adapter.flow.lib.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.vality.adapter.common.state.deserializer.DeserializationException;
import dev.vality.adapter.common.state.deserializer.Deserializer;
import dev.vality.adapter.flow.lib.model.GeneralExitStateModel;

import java.io.IOException;

public class AdapterDeserializer implements Deserializer<GeneralExitStateModel> {
    private final ObjectMapper mapper;

    public GeneralExitStateModel read(byte[] data) {
        if (data == null) {
            return new GeneralExitStateModel();
        } else {
            try {
                return this.getMapper().readValue(data, GeneralExitStateModel.class);
            } catch (IOException var3) {
                throw new IllegalArgumentException(var3);
            }
        }
    }

    public GeneralExitStateModel read(String data) {
        throw new DeserializationException("Deserialization not supported");
    }

    public ObjectMapper getMapper() {
        return this.mapper;
    }

    public AdapterDeserializer(ObjectMapper mapper) {
        this.mapper = mapper;
    }
}