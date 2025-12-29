package com.sigmundgranaas.forgero.mc.testcommon.assertions;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.loader.api.ForgeroApi;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Assertion utilities for ItemStack testing with fluent API.
 * Provides readable assertions for ItemStack validation and Forgero component conversion.
 *
 * <p>Example usage:
 * <pre>{@code
 * ItemStackAssertions.assertThat(stack)
 *     .isNotEmpty()
 *     .hasItem(Items.DIAMOND_PICKAXE)
 *     .hasCount(1)
 *     .hasTag()
 *     .convertsToComponent();
 *
 * // Get the component for further testing
 * Component component = ItemStackAssertions.assertThat(stack).asComponent();
 * }</pre>
 */
public final class ItemStackAssertions {

    private ItemStackAssertions() {
        // Prevent instantiation
    }

    /**
     * Start a fluent assertion chain on an ItemStack.
     *
     * @param stack the ItemStack to assert on
     * @return an ItemStackAssertion instance for fluent assertions
     */
    public static ItemStackAssertion assertThat(ItemStack stack) {
        return new ItemStackAssertion(stack);
    }

    /**
     * Fluent assertion API for ItemStack testing.
     */
    public static class ItemStackAssertion {
        private final ItemStack stack;

        ItemStackAssertion(ItemStack stack) {
            assertNotNull(stack, "ItemStack should not be null");
            this.stack = stack;
        }

        // ========== Basic ItemStack Assertions ==========

        /**
         * Asserts that the ItemStack is not empty.
         *
         * @return this assertion for chaining
         */
        public ItemStackAssertion isNotEmpty() {
            assertFalse(stack.isEmpty(), "ItemStack should not be empty");
            return this;
        }

        /**
         * Asserts that the ItemStack is empty.
         *
         * @return this assertion for chaining
         */
        public ItemStackAssertion isEmpty() {
            assertTrue(stack.isEmpty(), "ItemStack should be empty");
            return this;
        }

        /**
         * Asserts that the ItemStack has the specified item type.
         *
         * @param item the expected item
         * @return this assertion for chaining
         */
        public ItemStackAssertion hasItem(Item item) {
            assertEquals(item, stack.getItem(),
                    "ItemStack should have item " + item + " but has " + stack.getItem());
            return this;
        }

        /**
         * Asserts that the ItemStack has the specified count.
         *
         * @param count the expected count
         * @return this assertion for chaining
         */
        public ItemStackAssertion hasCount(int count) {
            assertEquals(count, stack.getCount(),
                    "ItemStack should have count " + count + " but has " + stack.getCount());
            return this;
        }

        /**
         * Asserts that the ItemStack count is at least the specified value.
         *
         * @param minCount the minimum count
         * @return this assertion for chaining
         */
        public ItemStackAssertion hasAtLeast(int minCount) {
            assertTrue(stack.getCount() >= minCount,
                    "ItemStack should have at least " + minCount + " but has " + stack.getCount());
            return this;
        }

        /**
         * Asserts that the ItemStack count is at most the specified value.
         *
         * @param maxCount the maximum count
         * @return this assertion for chaining
         */
        public ItemStackAssertion hasAtMost(int maxCount) {
            assertTrue(stack.getCount() <= maxCount,
                    "ItemStack should have at most " + maxCount + " but has " + stack.getCount());
            return this;
        }

        // ========== NBT Assertions ==========

        /**
         * Asserts that the ItemStack has NBT data.
         *
         * @return this assertion for chaining
         */
        public ItemStackAssertion hasTag() {
            assertTrue(stack.hasNbt(), "ItemStack should have NBT tag");
            return this;
        }

        /**
         * Asserts that the ItemStack does not have NBT data.
         *
         * @return this assertion for chaining
         */
        public ItemStackAssertion hasNoTag() {
            assertFalse(stack.hasNbt(), "ItemStack should not have NBT tag");
            return this;
        }

        /**
         * Asserts that the ItemStack's NBT contains the specified key.
         *
         * @param key the NBT key to check
         * @return this assertion for chaining
         */
        public ItemStackAssertion tagContains(String key) {
            assertTrue(stack.hasNbt(), "ItemStack should have NBT tag");
            assertTrue(stack.getNbt().contains(key),
                    "ItemStack NBT should contain key '" + key + "'");
            return this;
        }

        /**
         * Asserts that the ItemStack's NBT does not contain the specified key.
         *
         * @param key the NBT key to check
         * @return this assertion for chaining
         */
        public ItemStackAssertion tagDoesNotContain(String key) {
            if (stack.hasNbt()) {
                assertFalse(stack.getNbt().contains(key),
                        "ItemStack NBT should not contain key '" + key + "'");
            }
            return this;
        }

        /**
         * Asserts that the ItemStack's NBT contains the specified string value.
         *
         * @param key the NBT key
         * @param value the expected string value
         * @return this assertion for chaining
         */
        public ItemStackAssertion tagHasString(String key, String value) {
            tagContains(key);
            assertEquals(value, stack.getNbt().getString(key),
                    "NBT key '" + key + "' should have value '" + value + "'");
            return this;
        }

        /**
         * Asserts that the ItemStack's NBT contains the specified int value.
         *
         * @param key the NBT key
         * @param value the expected int value
         * @return this assertion for chaining
         */
        public ItemStackAssertion tagHasInt(String key, int value) {
            tagContains(key);
            assertEquals(value, stack.getNbt().getInt(key),
                    "NBT key '" + key + "' should have value " + value);
            return this;
        }

        // ========== Durability Assertions ==========

        /**
         * Asserts that the ItemStack is damageable.
         *
         * @return this assertion for chaining
         */
        public ItemStackAssertion isDamageable() {
            assertTrue(stack.isDamageable(), "ItemStack should be damageable");
            return this;
        }

        /**
         * Asserts that the ItemStack is not damageable.
         *
         * @return this assertion for chaining
         */
        public ItemStackAssertion isNotDamageable() {
            assertFalse(stack.isDamageable(), "ItemStack should not be damageable");
            return this;
        }

        /**
         * Asserts that the ItemStack has the specified damage value.
         *
         * @param damage the expected damage
         * @return this assertion for chaining
         */
        public ItemStackAssertion hasDamage(int damage) {
            assertEquals(damage, stack.getDamage(),
                    "ItemStack should have damage " + damage + " but has " + stack.getDamage());
            return this;
        }

        /**
         * Asserts that the ItemStack has the specified max damage.
         *
         * @param maxDamage the expected max damage
         * @return this assertion for chaining
         */
        public ItemStackAssertion hasMaxDamage(int maxDamage) {
            assertEquals(maxDamage, stack.getMaxDamage(),
                    "ItemStack should have max damage " + maxDamage + " but has " + stack.getMaxDamage());
            return this;
        }

        // ========== Forgero Component Conversion Assertions ==========

        /**
         * Asserts that the ItemStack can be converted to a Forgero Component.
         *
         * @return this assertion for chaining
         */
        public ItemStackAssertion convertsToComponent() {
            Optional<Component> component = ForgeroApi.services().converter().toComponent(stack);
            assertTrue(component.isPresent(),
                    "ItemStack should convert to a Forgero Component");
            return this;
        }

        /**
         * Asserts that the ItemStack cannot be converted to a Forgero Component.
         *
         * @return this assertion for chaining
         */
        public ItemStackAssertion doesNotConvertToComponent() {
            Optional<Component> component = ForgeroApi.services().converter().toComponent(stack);
            assertFalse(component.isPresent(),
                    "ItemStack should not convert to a Forgero Component");
            return this;
        }

        /**
         * Converts the ItemStack to a Component.
         * The ItemStack must be convertible to a Component.
         *
         * @return the converted Component
         */
        public Component asComponent() {
            convertsToComponent();
            return ForgeroApi.services().converter().toComponent(stack).get();
        }

        /**
         * Returns the underlying ItemStack.
         *
         * @return the ItemStack being asserted on
         */
        public ItemStack get() {
            return stack;
        }
    }
}
