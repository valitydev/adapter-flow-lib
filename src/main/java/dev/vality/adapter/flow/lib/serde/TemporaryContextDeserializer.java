package dev.vality.adapter.flow.lib.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.vality.adapter.flow.lib.exception.DeserializationException;
import dev.vality.adapter.flow.lib.model.TemporaryContext;

import java.io.IOException;

public class TemporaryContextDeserializer implements Deserializer<TemporaryContext> {

    private final ObjectMapper mapper;

    public TemporaryContext read(byte[] data) {
        if (data == null) {
            return new TemporaryContext();
        } else {
            try {
                return this.getMapper().readValue(data, TemporaryContext.class);
            } catch (IOException var3) {
                throw new IllegalArgumentException(var3);
            }
        }
    }

    public TemporaryContext read(String data) {
        throw new DeserializationException("Deserialization not supported");
    }

    public ObjectMapper getMapper() {
        return this.mapper;
    }

    public TemporaryContextDeserializer(ObjectMapper mapper) {
        this.mapper = mapper;
    }

}