package com.sigmundgranaas.forgero.item.forgerotool.material;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

class MaterialCollectionTest {

    @Test
    void LoadMaterialsAsList() {
        MaterialCollection materialCollection = new MaterialCollectionImpl(MaterialLoader.INSTANCE);
        assert (materialCollection.getMaterialsAsList().size() > 0);
    }

    @Test
    void LoadMaterialsAsMap() {
        MaterialCollection materialCollection = new MaterialCollectionImpl(MaterialLoader.INSTANCE);
        assert (materialCollection.getMaterialsAsMap().size() > 0);
    }

    @Test
    void getPrimaryMaterialsAsList() {
        fail();
    }

    @Test
    void getSecondaryMaterialsAsList() {
        fail();
    }

    @Test
    void getMaterial() {
        fail();
    }

    @Test
    void testGetMaterial() {
        fail();
    }

    @Test
    void testGetMaterial1() {
        fail();
    }
}