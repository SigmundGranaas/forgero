package com.sigmundgranaas.forgero.core.toolpart.factory;

import com.sigmundgranaas.forgero.core.material.material.EmptySecondaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.MaterialType;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.realistic.RealisticDuoMaterial;
import com.sigmundgranaas.forgero.core.material.material.realistic.RealisticMaterialPOJO;
import com.sigmundgranaas.forgero.core.material.material.simple.SimpleDuoMaterial;
import com.sigmundgranaas.forgero.core.material.material.simple.SimpleMaterialPOJO;
import com.sigmundgranaas.forgero.core.properties.Property;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.toolpart.strategy.HandleMaterialStrategy;
import com.sigmundgranaas.forgero.core.toolpart.strategy.realistic.RealisticBindingStrategy;
import com.sigmundgranaas.forgero.core.toolpart.strategy.realistic.RealisticHandleStrategy;
import com.sigmundgranaas.forgero.core.toolpart.strategy.realistic.RealisticHeadStrategy;
import com.sigmundgranaas.forgero.core.toolpart.strategy.simple.SimpleBindingStrategy;
import com.sigmundgranaas.forgero.core.toolpart.strategy.simple.SimpleHandleStrategy;
import com.sigmundgranaas.forgero.core.toolpart.strategy.simple.SimpleMaterialHeadStrategy;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class ToolPartStrategyFactoryTest {

    @Test
    void createRealisticToolPartHeadStrategy() {
        var strategy = ToolPartStrategyFactory.createToolPartHeadStrategy(ForgeroToolTypes.PICKAXE, new RealisticDuoMaterial(RealisticMaterialPOJO.createDefaultMaterialPOJO()));
        Assertions.assertEquals(RealisticHeadStrategy.class, strategy.getClass());
    }

    @Test
    void createSimpleToolPartHeadStrategy() {
        var strategy = ToolPartStrategyFactory.createToolPartHeadStrategy(ForgeroToolTypes.PICKAXE, new SimpleDuoMaterial(SimpleMaterialPOJO.createDefaultMaterialPOJO()));
        Assertions.assertEquals(SimpleMaterialHeadStrategy.class, strategy.getClass());
    }

    @Test
    void createRealisticToolPartHandleStrategy() {
        var strategy = ToolPartStrategyFactory.createToolPartHandleStrategy(new RealisticDuoMaterial(RealisticMaterialPOJO.createDefaultMaterialPOJO()));
        Assertions.assertEquals(RealisticHandleStrategy.class, strategy.getClass());
    }

    @Test
    void createSimpleToolPartHandleStrategy() {
        HandleMaterialStrategy strategy = ToolPartStrategyFactory.createToolPartHandleStrategy(new SimpleDuoMaterial(SimpleMaterialPOJO.createDefaultMaterialPOJO()));
        Assertions.assertEquals(SimpleHandleStrategy.class, strategy.getClass());
    }

    @Test
    void createRealisticToolPartBindingStrategy() {
        var strategy = ToolPartStrategyFactory.createToolPartBinding(new RealisticDuoMaterial(RealisticMaterialPOJO.createDefaultMaterialPOJO()));
        Assertions.assertEquals(RealisticBindingStrategy.class, strategy.getClass());
    }

    @Test
    void createSimpleToolPartBindingStrategy() {
        var strategy = ToolPartStrategyFactory.createToolPartBinding(new SimpleDuoMaterial(SimpleMaterialPOJO.createDefaultMaterialPOJO()));
        Assertions.assertEquals(SimpleBindingStrategy.class, strategy.getClass());
    }

    @Test
    void ErrorOnUnknownMaterialTypeHead() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            ToolPartStrategyFactory.createToolPartHeadStrategy(ForgeroToolTypes.PICKAXE, new PrimaryMaterialTestClass(), new EmptySecondaryMaterial());
        });
    }

    @Test
    void ErrorOnUnknownMaterialTypeHandle() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            ToolPartStrategyFactory.createToolPartHandleStrategy(new PrimaryMaterialTestClass(), new EmptySecondaryMaterial());
        });
    }

    @Test
    void ErrorOnUnknownMaterialTypeBinding() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            ToolPartStrategyFactory.createToolPartBinding(new PrimaryMaterialTestClass(), new EmptySecondaryMaterial());
        });
    }

    public static class PrimaryMaterialTestClass implements PrimaryMaterial {

        @Override
        public int getRarity() {
            return 0;
        }

        @Override
        public @NotNull
        String getName() {
            return null;
        }

        @Override
        public int getDurability() {
            return 0;
        }

        @Override
        public MaterialType getType() {
            return null;
        }

        @Override
        public @NotNull
        List<Property> getProperties() {
            return null;
        }

        @Override
        public String getIngredient() {
            return null;
        }

    }
}