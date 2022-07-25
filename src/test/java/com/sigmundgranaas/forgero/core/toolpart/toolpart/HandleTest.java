package com.sigmundgranaas.forgero.core.toolpart.toolpart;

import com.sigmundgranaas.forgero.Constants;
import com.sigmundgranaas.forgero.core.ForgeroRegistry;
import com.sigmundgranaas.forgero.core.gem.EmptyGem;
import com.sigmundgranaas.forgero.core.material.material.EmptySecondaryMaterial;
import com.sigmundgranaas.forgero.core.resourceloader.TestResourceLoader;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.core.toolpart.handle.Handle;
import com.sigmundgranaas.forgero.core.toolpart.handle.HandleState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.sigmundgranaas.forgero.Util.INIT_TEST_REGISTRY;
import static com.sigmundgranaas.forgero.core.property.ToolPropertyTest.HANDLE_SCHEMATIC;

public class HandleTest {
    @BeforeAll
    static void initialiseResources() {
        INIT_TEST_REGISTRY.run();
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