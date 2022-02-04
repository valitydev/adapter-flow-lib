package dev.vality.adapter.flow.lib.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.vality.adapter.common.state.deserializer.DeserializationException;
import dev.vality.adapter.common.state.deserializer.Deserializer;
import dev.vality.adapter.flow.lib.model.ThreeDsV2Callback;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ThreeDsV2CallbackDeserializer implements Deserializer<ThreeDsV2Callback> {

    private final ObjectMapper mapper;

    public ThreeDsV2Callback read(byte[] data) {
        if (data == null) {
            return new ThreeDsV2Callback();
        } else {
            try {
                return mapper.readValue(data, ThreeDsV2Callback.class);
            } catch (IOException var3) {
                throw new IllegalArgumentException(var3);
            }
        }
    }

    public ThreeDsV2Callback read(String data) {
        throw new DeserializationException("Deserialization not supported");
    }

    public ThreeDsV2Callback read(HttpServletRequest request) {
        Map<String, String> stringMap = Optional.ofNullable(request.getParameterMap())
                .orElseGet(HashMap::new)
                .entrySet().stream()
                .collect(Collectors.toMap(k -> k.getKey().trim(),
                        v -> v.getValue()[0]));
        return mapper.convertValue(stringMap, ThreeDsV2Callback.class);
    }

}