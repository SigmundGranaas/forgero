package com.sigmundgranaas.forgero.core.material.material.factory;

import com.sigmundgranaas.forgero.core.material.material.ForgeroMaterial;
import com.sigmundgranaas.forgero.core.material.material.MaterialPOJO;
import com.sigmundgranaas.forgero.core.material.material.MaterialType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MaterialFactoryImplTest {
    public static final ForgeroMaterial testMaterial = new MaterialFactoryImpl().createMaterial(MaterialPOJO.createDefaultMaterialPOJO());

    @Test
    void materialName() {
        Assertions.assertEquals("default", testMaterial.getName());
    }

    @Test
    void rarity() {
        Assertions.assertEquals(1, testMaterial.getRarity());
    }

    @Test
    void materialType() {
        Assertions.assertEquals(MaterialType.METAL, testMaterial.getType());
    }

    @Test
    void durability() {
        Assertions.assertEquals(1, testMaterial.getDurability());
    }

    @Test
    void properties() {
        Assertions.assertEquals(0, testMaterial.getProperties().size());
    }

    @Test
    void weight() {
        Assertions.assertEquals(1, testMaterial.getWeight());
    }

    @Test
    void paletteInclusions() {
        Assertions.assertEquals(0, testMaterial.getPaletteIdentifiers().size());
    }

    @Test
    void paletteExclusions() {
        Assertions.assertEquals(0, testMaterial.getPaletteExclusionIdentifiers().size());
    }

    @Test
    void ingredient() {
        Assertions.assertEquals("ingredient", testMaterial.getIngredientAsString());
    }
}