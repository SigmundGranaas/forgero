package com.sigmundgranaas.forgero;

import com.sigmundgranaas.forgero.type.TypeTree;
import org.junit.jupiter.api.Test;

import java.util.Collections;

public class ResourceRegistryTest {

    @Test
    void testGetPickaxe() {
        var registry = ResourceRegistry.of(Collections.emptyMap(), new TypeTree());
        registry.get("test");
    }

    @Test
    void testGetPickaxeType() {

    }
}
