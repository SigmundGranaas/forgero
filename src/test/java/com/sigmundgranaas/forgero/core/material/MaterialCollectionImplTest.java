package com.sigmundgranaas.forgero.core.material;

import com.sigmundgranaas.forgero.Constants;
import com.sigmundgranaas.forgero.core.identifier.ForgeroMaterialIdentifierImpl;
import com.sigmundgranaas.forgero.core.identifier.ForgeroToolIdentifierImpl;
import com.sigmundgranaas.forgero.core.identifier.ForgeroToolPartIdentifierImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MaterialCollectionImplTest {

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
        MaterialCollection materialCollection = new MaterialCollectionImpl(MaterialLoader.INSTANCE);
        materialCollection.getPrimaryMaterialsAsList().forEach(Assertions::assertNotNull);
    }

    @Test
    void getSecondaryMaterialsAsList() {
        MaterialCollection materialCollection = new MaterialCollectionImpl(MaterialLoader.INSTANCE);
        materialCollection.getSecondaryMaterialsAsList().forEach(Assertions::assertNotNull);
    }

    @Test
    void getMaterialFromToolIdentifier() {
        MaterialCollection materialCollection = new MaterialCollectionImpl(MaterialLoader.INSTANCE);
        materialCollection.getMaterial(new ForgeroToolIdentifierImpl(Constants.EXAMPLE_TOOL_IDENTIFIER));

    }

    @Test
    void getMaterialFromToolPartIdentifier() {
        MaterialCollection materialCollection = new MaterialCollectionImpl(MaterialLoader.INSTANCE);
        materialCollection.getMaterial(new ForgeroToolPartIdentifierImpl(Constants.IRON_PICKAXEHEAD_IDENTIFIER));

    }

    @Test
    void getMaterialFromMaterialIdentifier() {
        MaterialCollection materialCollection = new MaterialCollectionImpl(MaterialLoader.INSTANCE);
        materialCollection.getMaterial(new ForgeroMaterialIdentifierImpl(Constants.IRON_IDENTIFIER_STRING));
    }
}