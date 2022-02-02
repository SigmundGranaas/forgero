package com.sigmundgranaas.forgero.core.tool.toolpart.head;

import com.sigmundgranaas.forgero.core.material.material.realistic.RealisticDuoMaterial;
import com.sigmundgranaas.forgero.core.material.material.realistic.RealisticMaterialPOJO;
import com.sigmundgranaas.forgero.core.tool.toolpart.strategy.realistic.RealisticHeadStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SimpleHeadStrategyTest {

    RealisticHeadStrategy strategy;

    @BeforeEach
    void setUpDefaultMaterial() {
        strategy = new RealisticHeadStrategy(new RealisticDuoMaterial(RealisticMaterialPOJO.createDefaultMaterialPOJO()));
    }


    @Test
    void getDurability() {
    }

    @Test
    void getDamage() {
    }

    @Test
    void getMiningSpeedMultiplier() {
    }

    @Test
    void getMiningLevel() {
    }
}