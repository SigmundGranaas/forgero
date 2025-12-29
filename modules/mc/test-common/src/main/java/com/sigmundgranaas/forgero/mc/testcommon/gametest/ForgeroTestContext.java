package com.sigmundgranaas.forgero.mc.testcommon.gametest;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.loader.api.ForgeroApi;
import com.sigmundgranaas.forgero.loader.api.ForgeroServices;
import net.minecraft.util.math.BlockPos;
import net.minecraft.test.TestContext;
import net.minecraft.item.ItemStack;
import net.minecraft.block.entity.BlockEntity;

import java.util.Optional;

/**
 * Enhanced GameTest context with Forgero-specific utilities.
 * Wraps a TestContext and provides convenient access to Forgero functionality.
 *
 * <p>This class delegates all standard GameTest operations to the underlying context
 * while adding Forgero-specific methods for component lookup, conversion, and testing.
 *
 * <p>Example usage:
 * <pre>{@code
 * ForgeroTestContext ctx = new ForgeroTestContext(context);
 *
 * // Component lookup
 * Optional<Component> comp = ctx.component("forgero:iron_pickaxe_head");
 *
 * // ItemStack <-> Component conversion
 * ItemStack stack = ctx.toStack(comp.get()).orElseThrow();
 * Component converted = ctx.toComponent(stack).orElseThrow();
 *
 * // Standard GameTest operations still work
 * ctx.assertTrue(comp.isPresent(), "Component should exist");
 * ctx.complete();
 * }</pre>
 */
public class ForgeroTestContext {

    private final TestContext context;
    private final ForgeroServices api;

    /**
     * Creates a new ForgeroTestContext wrapping the given TestContext.
     *
     * @param context the TestContext to wrap
     */
    public ForgeroTestContext(TestContext context) {
        this.context = context;
        this.api = ForgeroApi.services();
    }

    // ========== Forgero-Specific Methods ==========

    /**
     * Looks up a component by identifier from the registry.
     *
     * @param identifier the component identifier (e.g., "forgero:iron_pickaxe_head")
     * @return an Optional containing the component if found
     */
    public Optional<Component> component(String identifier) {
        return api.componentRegistry().get(OpenIdentifier.parse(identifier));
    }

    /**
     * Looks up a component by identifier from the registry.
     *
     * @param identifier the component identifier
     * @return an Optional containing the component if found
     */
    public Optional<Component> component(OpenIdentifier identifier) {
        return api.componentRegistry().get(identifier);
    }

    /**
     * Converts a Component to an ItemStack.
     *
     * @param component the component to convert
     * @return an Optional containing the ItemStack if conversion succeeded
     */
    public Optional<ItemStack> toStack(Component component) {
        return api.converter().toStack(component);
    }

    /**
     * Converts an ItemStack to a Component.
     *
     * @param stack the ItemStack to convert
     * @return an Optional containing the Component if conversion succeeded
     */
    public Optional<Component> toComponent(ItemStack stack) {
        return api.converter().toComponent(stack);
    }

    /**
     * Gets the ForgeroServices instance for direct service access.
     *
     * @return the ForgeroServices instance
     */
    public ForgeroServices api() {
        return api;
    }

    // ========== Delegated GameTestHelper Methods ==========

    /**
     * Gets the underlying TestContext.
     *
     * @return the wrapped TestContext
     */
    public TestContext context() {
        return context;
    }

    /**
     * Asserts that a condition is true.
     *
     * @param condition the condition to check
     * @param message the failure message
     */
    public void assertTrue(boolean condition, String message) {
        context.assertTrue(condition, message);
    }

    /**
     * Asserts that a condition is false.
     *
     * @param condition the condition to check
     * @param message the failure message
     */
    public void assertFalse(boolean condition, String message) {
        context.assertFalse(condition, message);
    }

    /**
     * Marks the test as successful and completes it.
     */
    public void complete() {
        context.complete();
    }

    /**
     * Gets a block entity at the specified position.
     *
     * @param pos the block position
     * @return the block entity at that position
     */
    public BlockEntity getBlockEntity(BlockPos pos) {
        return context.getWorld().getBlockEntity(context.getAbsolutePos(pos));
    }

    /**
     * Gets the absolute position for a relative position within the test structure.
     *
     * @param pos the relative position
     * @return the absolute position
     */
    public BlockPos getAbsolutePos(BlockPos pos) {
        return context.getAbsolutePos(pos);
    }

    /**
     * Gets the relative position for an absolute position.
     *
     * @param pos the absolute position
     * @return the relative position within the test structure
     */
    public BlockPos getRelativePos(BlockPos pos) {
        return context.getRelativePos(pos);
    }
}
