package dev.vality.adapter.flow.lib.serde;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.vality.adapter.flow.lib.exception.DeserializationException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

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
            } catch (IOException ex) {
                throw new IllegalArgumentException(ex);
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