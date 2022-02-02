package com.sigmundgranaas.forgero.core.material.material.realistic;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RealisticMaterialPOJOTest {
    @Test
    void CreateDummyMaterialPOJO() {
        Assertions.assertEquals("Default", RealisticMaterialPOJO.createDefaultMaterialPOJO().name);
    }
}
