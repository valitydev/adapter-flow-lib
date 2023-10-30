package dev.vality.adapter.flow.lib.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LocalIdGeneratorTest {

    public static final String TEST = "test";
    public static final String TEST_2 = "test_2";

    @Test
    void get() {
        LocalIdGenerator localIdGenerator = new LocalIdGenerator();

        Long idTest_1 = localIdGenerator.get(TEST);
        Long idTest_2 = localIdGenerator.get(TEST);

        Assertions.assertEquals(idTest_1, idTest_2);

        Long idTest_3 = localIdGenerator.get(TEST_2);

        Assertions.assertNotEquals(idTest_1, idTest_3);
    }
}