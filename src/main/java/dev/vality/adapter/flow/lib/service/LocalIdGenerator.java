package dev.vality.adapter.flow.lib.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RequiredArgsConstructor
public class LocalIdGenerator implements IdGenerator {

    @SneakyThrows
    @Override
    public Long get(String invoiceId) {
        return UUID.nameUUIDFromBytes(invoiceId.getBytes(StandardCharsets.UTF_8)).getMostSignificantBits() &
                Long.MAX_VALUE;
    }
}
