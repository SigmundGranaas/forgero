package com.sigmundgranaas.forgero.mc.testcommon;

import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroTestContext;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

/**
 * GameTests for ForgeroGameTest interface and ForgeroTestContext wrapper.
 */
public class ForgeroContextTests implements ForgeroGameTest {

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void services_shouldProvideValidForgeroServices(TestContext context) {
        var services = services();

        context.assertTrue(services != null, "ForgeroServices should not be null");
        context.assertTrue(services.componentRegistry() != null, "ComponentRegistry should not be null");
        context.assertTrue(services.converter() != null, "Converter should not be null");
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void forgero_shouldWrapContext(TestContext context) {
        ForgeroTestContext ctx = forgero(context);

        context.assertTrue(ctx != null, "ForgeroTestContext should not be null");
        context.assertTrue(ctx.context() == context, "Should return the original context");
        context.assertTrue(ctx.api() != null, "Should provide ForgeroServices");
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void context_assertionMethods_shouldDelegate(TestContext context) {
        ForgeroTestContext ctx = forgero(context);

        // Test assertTrue - should not throw
        ctx.assertTrue(true, "True condition should pass");

        // Test assertFalse - should not throw
        ctx.assertFalse(false, "False condition should pass");

        // Test complete - should not throw
        ctx.complete();
    }
}
