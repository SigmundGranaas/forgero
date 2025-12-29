package com.sigmundgranaas.forgero.mc.testcommon;

import com.sigmundgranaas.forgero.mc.testcommon.assertions.ItemStackAssertions;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ItemStackAssertions fluent API.
 */
class ItemStackAssertionsTests {

    @Test
    void assertThat_withNonNullStack_shouldReturnAssertion() {
        ItemStack stack = new ItemStack(Items.DIAMOND);
        var assertion = ItemStackAssertions.assertThat(stack);
        assertNotNull(assertion, "Should return assertion instance");
    }

    @Test
    void isNotEmpty_withNonEmptyStack_shouldPass() {
        ItemStack stack = new ItemStack(Items.DIAMOND);
        assertDoesNotThrow(() ->
                ItemStackAssertions.assertThat(stack).isNotEmpty()
        );
    }

    @Test
    void isEmpty_withEmptyStack_shouldPass() {
        ItemStack stack = ItemStack.EMPTY;
        assertDoesNotThrow(() ->
                ItemStackAssertions.assertThat(stack).isEmpty()
        );
    }

    @Test
    void hasItem_withMatchingItem_shouldPass() {
        ItemStack stack = new ItemStack(Items.DIAMOND_PICKAXE);
        assertDoesNotThrow(() ->
                ItemStackAssertions.assertThat(stack).hasItem(Items.DIAMOND_PICKAXE)
        );
    }

    @Test
    void hasCount_withMatchingCount_shouldPass() {
        ItemStack stack = new ItemStack(Items.DIAMOND, 32);
        assertDoesNotThrow(() ->
                ItemStackAssertions.assertThat(stack).hasCount(32)
        );
    }

    @Test
    void hasAtLeast_withSufficientCount_shouldPass() {
        ItemStack stack = new ItemStack(Items.DIAMOND, 10);
        assertDoesNotThrow(() ->
                ItemStackAssertions.assertThat(stack).hasAtLeast(5)
        );
    }

    @Test
    void hasAtMost_withinLimit_shouldPass() {
        ItemStack stack = new ItemStack(Items.DIAMOND, 10);
        assertDoesNotThrow(() ->
                ItemStackAssertions.assertThat(stack).hasAtMost(20)
        );
    }

    @Test
    void hasTag_withNbt_shouldPass() {
        ItemStack stack = new ItemStack(Items.DIAMOND);
        stack.setNbt(new NbtCompound());
        assertDoesNotThrow(() ->
                ItemStackAssertions.assertThat(stack).hasTag()
        );
    }

    @Test
    void hasNoTag_withoutNbt_shouldPass() {
        ItemStack stack = new ItemStack(Items.DIAMOND);
        assertDoesNotThrow(() ->
                ItemStackAssertions.assertThat(stack).hasNoTag()
        );
    }

    @Test
    void tagContains_withKey_shouldPass() {
        ItemStack stack = new ItemStack(Items.DIAMOND);
        NbtCompound nbt = new NbtCompound();
        nbt.putString("test_key", "test_value");
        stack.setNbt(nbt);

        assertDoesNotThrow(() ->
                ItemStackAssertions.assertThat(stack).tagContains("test_key")
        );
    }

    @Test
    void tagDoesNotContain_withoutKey_shouldPass() {
        ItemStack stack = new ItemStack(Items.DIAMOND);
        NbtCompound nbt = new NbtCompound();
        nbt.putString("other_key", "value");
        stack.setNbt(nbt);

        assertDoesNotThrow(() ->
                ItemStackAssertions.assertThat(stack).tagDoesNotContain("test_key")
        );
    }

    @Test
    void tagHasString_withMatchingValue_shouldPass() {
        ItemStack stack = new ItemStack(Items.DIAMOND);
        NbtCompound nbt = new NbtCompound();
        nbt.putString("name", "custom_name");
        stack.setNbt(nbt);

        assertDoesNotThrow(() ->
                ItemStackAssertions.assertThat(stack).tagHasString("name", "custom_name")
        );
    }

    @Test
    void tagHasInt_withMatchingValue_shouldPass() {
        ItemStack stack = new ItemStack(Items.DIAMOND);
        NbtCompound nbt = new NbtCompound();
        nbt.putInt("level", 5);
        stack.setNbt(nbt);

        assertDoesNotThrow(() ->
                ItemStackAssertions.assertThat(stack).tagHasInt("level", 5)
        );
    }

    @Test
    void isDamageable_withDamageableItem_shouldPass() {
        ItemStack stack = new ItemStack(Items.DIAMOND_PICKAXE);
        assertDoesNotThrow(() ->
                ItemStackAssertions.assertThat(stack).isDamageable()
        );
    }

    @Test
    void isNotDamageable_withNonDamageableItem_shouldPass() {
        ItemStack stack = new ItemStack(Items.DIAMOND);
        assertDoesNotThrow(() ->
                ItemStackAssertions.assertThat(stack).isNotDamageable()
        );
    }

    @Test
    void hasDamage_withMatchingDamage_shouldPass() {
        ItemStack stack = new ItemStack(Items.DIAMOND_PICKAXE);
        stack.setDamage(100);
        assertDoesNotThrow(() ->
                ItemStackAssertions.assertThat(stack).hasDamage(100)
        );
    }

    @Test
    void hasMaxDamage_withMatchingMaxDamage_shouldPass() {
        ItemStack stack = new ItemStack(Items.DIAMOND_PICKAXE);
        int maxDamage = stack.getMaxDamage();
        assertDoesNotThrow(() ->
                ItemStackAssertions.assertThat(stack).hasMaxDamage(maxDamage)
        );
    }

    @Test
    void chainedAssertions_shouldWork() {
        ItemStack stack = new ItemStack(Items.DIAMOND_PICKAXE, 1);
        stack.setDamage(50);

        assertDoesNotThrow(() ->
                ItemStackAssertions.assertThat(stack)
                        .isNotEmpty()
                        .hasItem(Items.DIAMOND_PICKAXE)
                        .hasCount(1)
                        .isDamageable()
                        .hasDamage(50)
        );
    }

    @Test
    void get_shouldReturnOriginalStack() {
        ItemStack stack = new ItemStack(Items.DIAMOND);
        ItemStack returned = ItemStackAssertions.assertThat(stack).get();
        assertSame(stack, returned, "Should return the original stack");
    }
}
