package com.sigmundgranaas.forgero.mc.testcommon.gametest;

import com.sigmundgranaas.forgero.loader.api.ForgeroApi;
import com.sigmundgranaas.forgero.loader.api.ForgeroServices;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.test.TestContext;

/**
 * Base interface for Forgero GameTests.
 * Provides convenient access to ForgeroServices and enhanced test context.
 *
 * <p>This is a local copy to avoid Fabric's Knot classloader issues with interface static methods
 * from remapped JARs. The actual implementation is in {@link ForgeroTestUtils}.
 *
 * <p>Example usage:
 * <pre>{@code
 * public class MyTest implements ForgeroGameTest {
 *
 *     @GameTest(templateName = EMPTY_STRUCTURE)
 *     public void test_something(TestContext context) {
 *         var ctx = ForgeroTestUtils.forgero(context);
 *         var component = ctx.component("forgero:iron_pickaxe_head");
 *         context.assertTrue(component.isPresent(), "Component should exist");
 *         context.complete();
 *     }
 * }
 * }</pre>
 */
public interface ForgeroGameTest extends FabricGameTest {

    static ForgeroServices services() {
        return ForgeroTestUtils.services();
    }

    static ForgeroTestContext forgero(TestContext context) {
        return ForgeroTestUtils.forgero(context);
    }
}
