package dev.vality.adapter.flow.lib.service;

import dev.vality.adapter.flow.lib.utils.AdapterProperties;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class TagManagementServiceImpl implements TagManagementService {

    private final AdapterProperties adapterProperties;

    @SneakyThrows
    @Override
    public String findTag(Map<String, String> parameters) {
        Optional<String> first = adapterProperties.getTagGeneratorFieldNames().stream()
                .filter(s -> StringUtils.hasText(parameters.get(s)))
                .findFirst();
        String tagId = parameters.get(first.get());
        return initTag(tagId);
    }

    @Override
    public String initTag(String tagId) {
        return adapterProperties.getTagPrefix() + tagId;
    }
}
