package com.sigmundgranaas.forgerocore;

import com.sigmundgranaas.forgerocore.gem.EmptyGem;
import com.sigmundgranaas.forgerocore.material.material.EmptySecondaryMaterial;

import com.sigmundgranaas.forgerocore.resourceloader.TestResourceLoader;
import com.sigmundgranaas.forgerocore.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgerocore.toolpart.handle.Handle;
import com.sigmundgranaas.forgerocore.toolpart.handle.HandleState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.sigmundgranaas.forgerocore.ToolPropertyTest.HANDLE_SCHEMATIC;


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