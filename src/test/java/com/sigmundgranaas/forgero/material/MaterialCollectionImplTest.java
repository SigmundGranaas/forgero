package com.sigmundgranaas.forgero.material;

import com.sigmundgranaas.forgero.Constants;
import com.sigmundgranaas.forgero.identifier.ForgeroMaterialIdentifierImpl;
import com.sigmundgranaas.forgero.identifier.ForgeroToolIdentifierImpl;
import com.sigmundgranaas.forgero.identifier.ForgeroToolPartIdentifierImpl;
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
        materialCollection.getMaterial(new ForgeroToolIdentifierImpl(Constants.FORGERO_TOOL_IDENTIFIER_STRING));

    }

    @Test
    void getMaterialFromToolPartIdentifier() {
        MaterialCollection materialCollection = new MaterialCollectionImpl(MaterialLoader.INSTANCE);
        materialCollection.getMaterial(new ForgeroToolPartIdentifierImpl(Constants.FORGERO_TOOL_PART_IDENTIFIER_STRING));

    }

    @Test
    void getMaterialFromMaterialIdentifier() {
        MaterialCollection materialCollection = new MaterialCollectionImpl(MaterialLoader.INSTANCE);
        materialCollection.getMaterial(new ForgeroMaterialIdentifierImpl(Constants.FORGERO_MATERIAL_IDENTIFIER_STRING));
    }
}