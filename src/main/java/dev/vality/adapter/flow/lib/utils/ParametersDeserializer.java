package dev.vality.adapter.flow.lib.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.vality.adapter.common.state.deserializer.DeserializationException;
import dev.vality.adapter.common.state.deserializer.Deserializer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ParametersDeserializer implements Deserializer<Map<String, String>> {

    private final ObjectMapper mapper;

    public Map<String, String> read(byte[] data) {
        if (data == null) {
            return new HashMap<>();
        } else {
            try {
                return mapper.readValue(data, new TypeReference<HashMap<String, String>>() {
                });
            } catch (IOException var3) {
                throw new IllegalArgumentException(var3);
            }
        }
    }

    public Map<String, String> read(String data) {
        throw new DeserializationException("Deserialization not supported");
    }

    public Map<String, String> read(HttpServletRequest request) {
        return Optional.ofNullable(request.getParameterMap())
                .orElseGet(HashMap::new)
                .entrySet().stream()
                .collect(Collectors.toMap(k -> k.getKey().trim(),
                        v -> v.getValue()[0]));
    }

}