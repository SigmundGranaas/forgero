package com.sigmundgranaas.forgero;

import com.sigmundgranaas.forgero.gem.EmptyGem;
import com.sigmundgranaas.forgero.material.material.EmptySecondaryMaterial;

import com.sigmundgranaas.forgero.resourceloader.TestResourceLoader;
import com.sigmundgranaas.forgero.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.toolpart.handle.Handle;
import com.sigmundgranaas.forgero.toolpart.handle.HandleState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.sigmundgranaas.forgero.ToolPropertyTest.HANDLE_SCHEMATIC;


public class HandleTest {
    @BeforeEach
    void initialiseResources() {
        ForgeroRegistry.INSTANCE.loadResourcesIfEmpty(new TestResourceLoader());
    }

    public static Handle createDefaultToolPartHandle() {
        HandleState state = new HandleState(ForgeroRegistry.MATERIAL.getPrimaryMaterial(Constants.IRON_IDENTIFIER_STRING).get(), new EmptySecondaryMaterial(), EmptyGem.createEmptyGem(), HANDLE_SCHEMATIC.get());

        return new Handle(state);
    }

    @Test
    void getToolPartName() {
        ForgeroToolPart referenceToolPart = createDefaultToolPartHandle();
        Assertions.assertEquals("handle", referenceToolPart.getToolPartName());
    }
}