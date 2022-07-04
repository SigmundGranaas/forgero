package com.sigmundgranaas.forgero.core.toolpart.state;

import org.junit.jupiter.api.BeforeAll;

import static com.sigmundgranaas.forgero.Util.INIT_TEST_REGISTRY;

public class ForgeroTest {
    @BeforeAll
    static void initialiseResources() {
        INIT_TEST_REGISTRY.run();
    }
}
