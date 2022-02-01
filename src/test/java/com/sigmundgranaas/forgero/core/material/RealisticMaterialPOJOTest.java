package com.sigmundgranaas.forgero.core.material;

import com.sigmundgranaas.forgero.core.material.material.realistic.RealisticMaterialPOJO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RealisticMaterialPOJOTest {
    @Test
    void CreateDummyMaterialPOJO() {
        Assertions.assertTrue(RealisticMaterialPOJO.createDefaultMaterialPOJO().name.equals("test"));
    }
}
