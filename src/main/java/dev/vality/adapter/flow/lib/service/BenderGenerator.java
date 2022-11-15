package dev.vality.adapter.flow.lib.service;

import dev.vality.bender.BenderSrv;
import dev.vality.bender.GenerationResult;
import dev.vality.bender.GenerationSchema;
import dev.vality.bender.SequenceSchema;
import dev.vality.msgpack.Nil;
import dev.vality.msgpack.Value;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class BenderGenerator implements IdGenerator {

    private static final String SEQ_ID = "orderId";

    private final BenderSrv.Iface benderClient;

    @org.springframework.beans.factory.annotation.Value("adapter.prefix")
    private String adapterPrefix;

    @SneakyThrows
    @Override
    public Long get(String invoiceId) {
        GenerationSchema schema = GenerationSchema.sequence(new SequenceSchema().setSequenceId(SEQ_ID));
        GenerationResult result = benderClient.generateID(adapterPrefix + invoiceId, schema, Value.nl(new Nil()));
        return Long.parseLong(result.getInternalId());
    }
}
