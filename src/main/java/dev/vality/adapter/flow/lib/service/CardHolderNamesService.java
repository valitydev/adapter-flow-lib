package dev.vality.adapter.flow.lib.service;

import dev.vality.adapter.flow.lib.utils.AdapterProperties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class CardHolderNamesService {

    @Getter
    private final List<String> cardHoldersNames;

    public CardHolderNamesService(AdapterProperties properties) throws IOException {
        if (properties.getCardHolderNamesFile() != null) {
            this.cardHoldersNames = Files.readAllLines(properties.getCardHolderNamesFile().getFile().toPath())
                    .stream()
                    .sorted()
                    .collect(Collectors.toList());
        } else {
            this.cardHoldersNames = List.of();
        }
    }

}