package dev.vality.adapter.flow.lib.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LocalIdGeneratorTest {

    public static final String TEST = "test";
    public static final String TEST_2 = "test_2";

    @Test
    void get() {
        LocalIdGenerator localIdGenerator = new LocalIdGenerator();

        Long idTest1 = localIdGenerator.get(TEST);
        Long idTest2 = localIdGenerator.get(TEST);

        Assertions.assertEquals(idTest1, idTest2);

        Long idTest3 = localIdGenerator.get(TEST_2);

        Assertions.assertNotEquals(idTest1, idTest3);
    }
}