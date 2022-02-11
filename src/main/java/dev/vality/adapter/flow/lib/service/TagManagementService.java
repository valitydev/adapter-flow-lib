package dev.vality.adapter.flow.lib.service;

import dev.vality.adapter.flow.lib.utils.AdapterProperties;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TagManagementService {

    private final AdapterProperties adapterProperties;

    @SneakyThrows
    public String findTag(Map<String, String> parameters) {
        Optional<String> first = adapterProperties.getTagGeneratorFieldNames().stream()
                .filter(s -> StringUtils.hasText(parameters.get(s)))
                .findFirst();
        return first.get();
    }

    @SneakyThrows
    public String get(Map<String, String> parameters) {
        Optional<String> first = adapterProperties.getTagGeneratorFieldNames().stream()
                .filter(s -> StringUtils.hasText(parameters.get(s)))
                .findFirst();
        return first.get();
    }


}
