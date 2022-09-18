package com.sigmundgranaas.forgero;

import com.sigmundgranaas.forgero.gem.EmptyGem;
import com.sigmundgranaas.forgero.material.material.EmptySecondaryMaterial;
import com.sigmundgranaas.forgero.resourceloader.TestResourceLoader;
import com.sigmundgranaas.forgero.toolpart.head.HeadState;
import com.sigmundgranaas.forgero.toolpart.head.PickaxeHead;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.sigmundgranaas.forgero.ToolPropertyTest.PICKAXEHEAD_SCHEMATIC;


public class PickaxeHeadTest {
    @BeforeEach
    void initialiseResources() {
        ForgeroRegistry.INSTANCE.loadResourcesIfEmpty(new TestResourceLoader());
    }

    public static PickaxeHead createDefaultPickaxeHead() {
        HeadState state = new HeadState(ForgeroRegistry.MATERIAL.getPrimaryMaterial(Constants.IRON_IDENTIFIER_STRING).get(), new EmptySecondaryMaterial(), EmptyGem.createEmptyGem(), PICKAXEHEAD_SCHEMATIC.get());
        return new PickaxeHead(state);
    }

    @Test
    void getToolTypeName() {
        Assertions.assertEquals("pickaxe", createDefaultPickaxeHead().getToolTypeName());
    }

    @Test
    void getToolPartName() {
        Assertions.assertEquals("pickaxehead", createDefaultPickaxeHead().getToolPartName());
    }
}