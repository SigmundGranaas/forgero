package com.sigmundgranaas.forgero.tests;

import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Single smoke test to verify the component system is functional.
 * This is the ONLY smoke test - all other tests must validate specific gameplay values.
 */
public class ComponentSmokeTest implements ForgeroGameTest {

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void component_system_is_functional(TestContext context) {
        var ctx = forgero(context);

        // Verify component registry is initialized
        assertNotNull(services().componentRegistry(), "Component registry must be initialized");

        // Verify components are loaded
        assertTrue(services().componentRegistry().all().size() > 0,
                "At least some components must be loaded");

        // Verify a known component exists (iron material)
        var iron = ctx.component("forgero:iron");
        assertTrue(iron.isPresent(), "Iron material must exist");

        // Verify a known vanilla tool exists
        var ironPickaxe = ctx.component("forgero:iron_pickaxe");
        assertTrue(ironPickaxe.isPresent(), "Iron pickaxe must exist");

        context.complete();
    }
}
