package com.sigmundgranaas.forgero.core.tool.toolpart.head;

import com.sigmundgranaas.forgero.core.material.material.realistic.RealisticDuoMaterial;
import com.sigmundgranaas.forgero.core.material.material.realistic.RealisticMaterialPOJO;
import com.sigmundgranaas.forgero.core.tool.toolpart.strategy.realistic.RealisticHeadStrategy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RealisticHeadStrategyTest {

    RealisticHeadStrategy strategy;

    public static RealisticMaterialPOJO setUpDefaultRealisticMaterial() {
        RealisticMaterialPOJO pojo = RealisticMaterialPOJO.createDefaultMaterialPOJO();
        RealisticMaterialPOJO.Primary primary = (RealisticMaterialPOJO.Primary) pojo.primary;
        primary.sharpness = 50;
        primary.stiffness = 50;
        pojo.durability = 50;
        return pojo;
    }

    @BeforeEach
    void setUpDefaultMaterial() {
        strategy = new RealisticHeadStrategy(new RealisticDuoMaterial(setUpDefaultRealisticMaterial()));
    }


    @Test
    void getDamage() {
        Assertions.assertEquals(5, strategy.getDamage());
    }

    @Test
    void getMiningSpeedMultiplier() {
        Assertions.assertEquals(2.5, strategy.getMiningSpeedMultiplier());
    }

    @Test
    void getMiningLevel() {
        Assertions.assertEquals(2, strategy.getMiningLevel());
    }
}