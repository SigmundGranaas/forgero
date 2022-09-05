package com.sigmundgranaas.forgerocore;



import com.sigmundgranaas.forgerocore.identifier.tool.ForgeroMaterialIdentifierImpl;
import com.sigmundgranaas.forgerocore.identifier.tool.ForgeroToolPartIdentifierImpl;
import com.sigmundgranaas.forgerocore.resourceloader.TestResourceLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MaterialCollectionImplTest {
    @BeforeEach
    void initialiseResources() {
        ForgeroRegistry.INSTANCE.loadResourcesIfEmpty(new TestResourceLoader());
    }

    @Test
    void LoadMaterialsAsList() {

        assert (ForgeroRegistry.MATERIAL.list().size() > 0);
    }

    @Test
    void LoadMaterialsAsMap() {

        assert (ForgeroRegistry.MATERIAL.getResourcesAsMap().size() > 0);
    }

    @Test
    void getPrimaryMaterialsAsList() {

        ForgeroRegistry.MATERIAL.list().forEach(Assertions::assertNotNull);
    }

    @Test
    void getSecondaryMaterialsAsList() {
        ForgeroRegistry.MATERIAL.getSecondaryMaterials().forEach(Assertions::assertNotNull);
    }

    @Test
    void getMaterialFromToolIdentifier() {
        var material = ForgeroRegistry.MATERIAL.getMaterial(new ForgeroToolPartIdentifierImpl(Constants.EXAMPLE_TOOL_IDENTIFIER));
        assert (material.isPresent());
    }

    @Test
    void getMaterialFromToolPartIdentifier() {
        var material = ForgeroRegistry.MATERIAL.getMaterial(new ForgeroToolPartIdentifierImpl(Constants.IRON_PICKAXEHEAD_IDENTIFIER));
        assert (material.isPresent());
    }

    @Test
    void getMaterialFromMaterialIdentifier() {
        var material = ForgeroRegistry.MATERIAL.getMaterial(new ForgeroMaterialIdentifierImpl(Constants.IRON_IDENTIFIER_STRING));
        assert (material.isPresent());
    }
}