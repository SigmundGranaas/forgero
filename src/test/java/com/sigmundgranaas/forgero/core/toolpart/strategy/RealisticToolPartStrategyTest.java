package com.sigmundgranaas.forgero.core.toolpart.strategy;

import com.sigmundgranaas.forgero.core.material.material.realistic.RealisticDuoMaterial;
import com.sigmundgranaas.forgero.core.toolpart.strategy.realistic.RealisticToolPartStrategy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.sigmundgranaas.forgero.core.toolpart.head.RealisticHeadStrategyTest.setUpDefaultRealisticMaterial;


class RealisticToolPartStrategyTest {
    RealisticHeadStrategyTestClass strategy;

    @BeforeEach
    void setUpDefaultMaterial() {
        strategy = new RealisticHeadStrategyTestClass(new RealisticDuoMaterial(setUpDefaultRealisticMaterial()));
    }

    @Test
    void getDurability() {
        Assertions.assertEquals(445, strategy.getDurability());
    }


    private static class RealisticHeadStrategyTestClass extends RealisticToolPartStrategy {
        public RealisticHeadStrategyTestClass(RealisticDuoMaterial realisticDuoMaterial) {
            super(realisticDuoMaterial);
        }
    }
}