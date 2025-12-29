package com.sigmundgranaas.forgero.mc.testcommon.gametest;

import com.sigmundgranaas.forgero.loader.api.ForgeroApi;
import com.sigmundgranaas.forgero.loader.api.ForgeroServices;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

/**
 * Base interface for Forgero GameTests.
 * Provides convenient access to ForgeroServices and enhanced test context.
 *
 * <p>Example usage:
 * <pre>{@code
 * public class MyTest implements ForgeroGameTest {
 *
 *     @GameTest(template = EMPTY_STRUCTURE)
 *     public void test_something(GameTestHelper helper) {
 *         // Easy access to ForgeroServices
 *         var registry = services().registry();
 *
 *         // Enhanced context with Forgero utilities
 *         var ctx = forgero(helper);
 *         var component = ctx.component("forgero:iron_pickaxe_head");
 *
 *         ctx.assertTrue(component.isPresent(), "Component should exist");
 *         ctx.succeed();
 *     }
 * }
 * }</pre>
 */
public interface ForgeroGameTest {

    /**
     * Common empty structure template for tests that don't need a specific structure.
     */
    String EMPTY_STRUCTURE = "minecraft:empty";

    /**
     * Gets the ForgeroServices instance.
     * Provides access to all Forgero services (registry, converter, resolver, etc.).
     *
     * @return the ForgeroServices instance
     */
    default ForgeroServices services() {
        return ForgeroApi.services();
    }

    /**
     * Wraps a TestContext in a ForgeroTestContext for enhanced functionality.
     * The ForgeroTestContext provides Forgero-specific utilities on top of the standard GameTest helpers.
     *
     * @param context the TestContext to wrap
     * @return an enhanced ForgeroTestContext
     */
    default ForgeroTestContext forgero(TestContext context) {
        return new ForgeroTestContext(context);
    }
}
