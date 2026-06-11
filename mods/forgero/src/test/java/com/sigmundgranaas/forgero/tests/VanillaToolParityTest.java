package com.sigmundgranaas.forgero.tests;

import com.sigmundgranaas.forgero.common.api.ForgeroApi;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroTestUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that validate Forgero vanilla tools have identical stats to Minecraft vanilla tools.
 * This is the baseline - all vanilla tools must match their Minecraft counterparts exactly.
 *
 * These tests are REQUIRED - if they fail, either:
 * 1. The attribute system is broken
 * 2. The vanilla tool configurations are incorrect
 *
 * IMPORTANT: These tests use the ItemQueryApi to get RESOLVED attribute values,
 * which is what Minecraft actually uses in-game. Testing raw component attributes
 * would miss composition bugs where multiple sources contribute to the same attribute.
 */
public class VanillaToolParityTest implements ForgeroGameTest {

    /**
     * Gets the resolved attribute value from an ItemStack using the proper resolution pipeline.
     * This returns the actual value that Minecraft will use in-game, not raw config values.
     */
    private float getResolvedAttribute(ItemStack stack, String attributeType) {
        return ForgeroApi.itemQuery().getAttribute(stack, OpenIdentifier.parse(attributeType));
    }

    /**
     * Converts a component to ItemStack and gets the resolved attribute value.
     * This is the proper way to test attribute values - through the full resolution pipeline.
     */
    private float getAttributeValue(Component component, String attributeType) {
        ItemStack stack = ForgeroApi.converter().toStack(component)
                .orElseThrow(() -> new AssertionError("Failed to convert component to ItemStack: " + component.id()));
        return getResolvedAttribute(stack, attributeType);
    }

    // ==================== Iron Tool Tests ====================

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void iron_pickaxe_has_vanilla_stats(TestContext context) {
        var ctx = ForgeroTestUtils.forgero(context);
        var pickaxe = ctx.component("forgero:iron_pickaxe");

        assertTrue(pickaxe.isPresent(), "Iron pickaxe must exist");

        // Validate exact vanilla Minecraft stats
        assertEquals(250f, getAttributeValue(pickaxe.get(), "forgero:durability"),
                "Iron pickaxe durability must be 250 (vanilla value)");
        assertEquals(4f, getAttributeValue(pickaxe.get(), "forgero:attack_damage"),
                "Iron pickaxe attack damage must be 4 (vanilla value)");
        assertEquals(6f, getAttributeValue(pickaxe.get(), "forgero:mining_speed"),
                "Iron pickaxe mining speed must be 6 (vanilla value)");
        assertEquals(2f, getAttributeValue(pickaxe.get(), "forgero:mining_level"),
                "Iron pickaxe mining level must be 2 (vanilla value)");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void iron_sword_has_vanilla_stats(TestContext context) {
        var ctx = ForgeroTestUtils.forgero(context);
        var sword = ctx.component("forgero:iron_sword");

        assertTrue(sword.isPresent(), "Iron sword must exist");

        // Validate exact vanilla Minecraft stats
        assertEquals(250f, getAttributeValue(sword.get(), "forgero:durability"),
                "Iron sword durability must be 250 (vanilla value)");
        assertEquals(6f, getAttributeValue(sword.get(), "forgero:attack_damage"),
                "Iron sword attack damage must be 6 (vanilla value)");
        assertEquals(1.6f, getAttributeValue(sword.get(), "forgero:attack_speed"),
                "Iron sword attack speed must be 1.6 (vanilla value)");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void iron_axe_has_vanilla_stats(TestContext context) {
        var ctx = ForgeroTestUtils.forgero(context);
        var axe = ctx.component("forgero:iron_axe");

        assertTrue(axe.isPresent(), "Iron axe must exist");

        // Validate exact vanilla Minecraft stats (Iron Axe: durability=250, damage=9, speed=0.9)
        assertEquals(250f, getAttributeValue(axe.get(), "forgero:durability"),
                "Iron axe durability must be 250 (vanilla value)");
        assertEquals(9f, getAttributeValue(axe.get(), "forgero:attack_damage"),
                "Iron axe attack damage must be 9 (vanilla value)");
        assertEquals(0.9f, getAttributeValue(axe.get(), "forgero:attack_speed"),
                "Iron axe attack speed must be 0.9 (vanilla value)");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void iron_shovel_has_vanilla_stats(TestContext context) {
        var ctx = ForgeroTestUtils.forgero(context);
        var shovel = ctx.component("forgero:iron_shovel");

        assertTrue(shovel.isPresent(), "Iron shovel must exist");

        // Validate exact vanilla Minecraft stats (Iron Shovel: durability=250, damage=4.5, speed=1.0)
        assertEquals(250f, getAttributeValue(shovel.get(), "forgero:durability"),
                "Iron shovel durability must be 250 (vanilla value)");
        assertEquals(4.5f, getAttributeValue(shovel.get(), "forgero:attack_damage"),
                "Iron shovel attack damage must be 4.5 (vanilla value)");
        assertEquals(1.0f, getAttributeValue(shovel.get(), "forgero:attack_speed"),
                "Iron shovel attack speed must be 1.0 (vanilla value)");

        context.complete();
    }

    // ==================== Diamond Tool Tests ====================

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void diamond_pickaxe_has_vanilla_stats(TestContext context) {
        var ctx = ForgeroTestUtils.forgero(context);
        var pickaxe = ctx.component("forgero:diamond_pickaxe");

        assertTrue(pickaxe.isPresent(), "Diamond pickaxe must exist");

        // Validate exact vanilla Minecraft stats
        assertEquals(1561f, getAttributeValue(pickaxe.get(), "forgero:durability"),
                "Diamond pickaxe durability must be 1561 (vanilla value)");
        assertEquals(5f, getAttributeValue(pickaxe.get(), "forgero:attack_damage"),
                "Diamond pickaxe attack damage must be 5 (vanilla value)");
        assertEquals(8f, getAttributeValue(pickaxe.get(), "forgero:mining_speed"),
                "Diamond pickaxe mining speed must be 8 (vanilla value)");
        assertEquals(3f, getAttributeValue(pickaxe.get(), "forgero:mining_level"),
                "Diamond pickaxe mining level must be 3 (vanilla value)");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void diamond_sword_has_vanilla_stats(TestContext context) {
        var ctx = ForgeroTestUtils.forgero(context);
        var sword = ctx.component("forgero:diamond_sword");

        assertTrue(sword.isPresent(), "Diamond sword must exist");

        // Validate exact vanilla Minecraft stats (Diamond Sword: durability=1561, damage=7, speed=1.6)
        assertEquals(1561f, getAttributeValue(sword.get(), "forgero:durability"),
                "Diamond sword durability must be 1561 (vanilla value)");
        assertEquals(7f, getAttributeValue(sword.get(), "forgero:attack_damage"),
                "Diamond sword attack damage must be 7 (vanilla value)");
        assertEquals(1.6f, getAttributeValue(sword.get(), "forgero:attack_speed"),
                "Diamond sword attack speed must be 1.6 (vanilla value)");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void diamond_axe_has_vanilla_stats(TestContext context) {
        var ctx = ForgeroTestUtils.forgero(context);
        var axe = ctx.component("forgero:diamond_axe");

        assertTrue(axe.isPresent(), "Diamond axe must exist");

        // Validate exact vanilla Minecraft stats (Diamond Axe: durability=1561, damage=9, speed=1.0)
        assertEquals(1561f, getAttributeValue(axe.get(), "forgero:durability"),
                "Diamond axe durability must be 1561 (vanilla value)");
        assertEquals(9f, getAttributeValue(axe.get(), "forgero:attack_damage"),
                "Diamond axe attack damage must be 9 (vanilla value)");
        assertEquals(1.0f, getAttributeValue(axe.get(), "forgero:attack_speed"),
                "Diamond axe attack speed must be 1.0 (vanilla value)");

        context.complete();
    }

    // ==================== Netherite Tool Tests ====================

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void netherite_pickaxe_has_vanilla_stats(TestContext context) {
        var ctx = ForgeroTestUtils.forgero(context);
        var pickaxe = ctx.component("forgero:netherite_pickaxe");

        assertTrue(pickaxe.isPresent(), "Netherite pickaxe must exist");

        // Validate exact vanilla Minecraft stats (Netherite Pickaxe: durability=2031, damage=6, speed=9, level=4)
        assertEquals(2031f, getAttributeValue(pickaxe.get(), "forgero:durability"),
                "Netherite pickaxe durability must be 2031 (vanilla value)");
        assertEquals(6f, getAttributeValue(pickaxe.get(), "forgero:attack_damage"),
                "Netherite pickaxe attack damage must be 6 (vanilla value)");
        assertEquals(9f, getAttributeValue(pickaxe.get(), "forgero:mining_speed"),
                "Netherite pickaxe mining speed must be 9 (vanilla value)");
        assertEquals(4f, getAttributeValue(pickaxe.get(), "forgero:mining_level"),
                "Netherite pickaxe mining level must be 4 (vanilla value)");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void netherite_sword_has_vanilla_stats(TestContext context) {
        var ctx = ForgeroTestUtils.forgero(context);
        var sword = ctx.component("forgero:netherite_sword");

        assertTrue(sword.isPresent(), "Netherite sword must exist");

        // Validate exact vanilla Minecraft stats (Netherite Sword: durability=2031, damage=8, speed=1.6)
        assertEquals(2031f, getAttributeValue(sword.get(), "forgero:durability"),
                "Netherite sword durability must be 2031 (vanilla value)");
        assertEquals(8f, getAttributeValue(sword.get(), "forgero:attack_damage"),
                "Netherite sword attack damage must be 8 (vanilla value)");
        assertEquals(1.6f, getAttributeValue(sword.get(), "forgero:attack_speed"),
                "Netherite sword attack speed must be 1.6 (vanilla value)");

        context.complete();
    }

    // ==================== Gold Tool Tests ====================

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void gold_pickaxe_has_vanilla_stats(TestContext context) {
        var ctx = ForgeroTestUtils.forgero(context);
        var pickaxe = ctx.component("forgero:golden_pickaxe");

        assertTrue(pickaxe.isPresent(), "Gold pickaxe must exist");

        // Validate exact vanilla Minecraft stats (Gold Pickaxe: durability=32, damage=2, speed=12, level=0)
        assertEquals(32f, getAttributeValue(pickaxe.get(), "forgero:durability"),
                "Gold pickaxe durability must be 32 (vanilla value)");
        assertEquals(2f, getAttributeValue(pickaxe.get(), "forgero:attack_damage"),
                "Gold pickaxe attack damage must be 2 (vanilla value)");
        assertEquals(12f, getAttributeValue(pickaxe.get(), "forgero:mining_speed"),
                "Gold pickaxe mining speed must be 12 (vanilla value - fastest)");
        assertEquals(0f, getAttributeValue(pickaxe.get(), "forgero:mining_level"),
                "Gold pickaxe mining level must be 0 (vanilla value)");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void gold_sword_has_vanilla_stats(TestContext context) {
        var ctx = ForgeroTestUtils.forgero(context);
        var sword = ctx.component("forgero:golden_sword");

        assertTrue(sword.isPresent(), "Gold sword must exist");

        // Validate exact vanilla Minecraft stats (Gold Sword: durability=32, damage=4, speed=1.6)
        assertEquals(32f, getAttributeValue(sword.get(), "forgero:durability"),
                "Gold sword durability must be 32 (vanilla value)");
        assertEquals(4f, getAttributeValue(sword.get(), "forgero:attack_damage"),
                "Gold sword attack damage must be 4 (vanilla value)");
        assertEquals(1.6f, getAttributeValue(sword.get(), "forgero:attack_speed"),
                "Gold sword attack speed must be 1.6 (vanilla value)");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void iron_hoe_has_vanilla_stats(TestContext context) {
        var ctx = ForgeroTestUtils.forgero(context);
        var hoe = ctx.component("forgero:iron_hoe");

        assertTrue(hoe.isPresent(), "Iron hoe must exist");

        // Validate exact vanilla Minecraft stats (Iron Hoe: durability=250, damage=1, speed=3.0)
        assertEquals(250f, getAttributeValue(hoe.get(), "forgero:durability"),
                "Iron hoe durability must be 250 (vanilla value)");
        assertEquals(1f, getAttributeValue(hoe.get(), "forgero:attack_damage"),
                "Iron hoe attack damage must be 1 (vanilla value - all hoes deal 1 damage)");
        assertEquals(3.0f, getAttributeValue(hoe.get(), "forgero:attack_speed"),
                "Iron hoe attack speed must be 3.0 (vanilla value)");

        context.complete();
    }

    // ==================== Diamond Tool Tests (continued) ====================

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void diamond_shovel_has_vanilla_stats(TestContext context) {
        var ctx = ForgeroTestUtils.forgero(context);
        var shovel = ctx.component("forgero:diamond_shovel");

        assertTrue(shovel.isPresent(), "Diamond shovel must exist");

        // Validate exact vanilla Minecraft stats (Diamond Shovel: durability=1561, damage=5.5, speed=1.0)
        assertEquals(1561f, getAttributeValue(shovel.get(), "forgero:durability"),
                "Diamond shovel durability must be 1561 (vanilla value)");
        assertEquals(5.5f, getAttributeValue(shovel.get(), "forgero:attack_damage"),
                "Diamond shovel attack damage must be 5.5 (vanilla value)");
        assertEquals(1.0f, getAttributeValue(shovel.get(), "forgero:attack_speed"),
                "Diamond shovel attack speed must be 1.0 (vanilla value)");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void diamond_hoe_has_vanilla_stats(TestContext context) {
        var ctx = ForgeroTestUtils.forgero(context);
        var hoe = ctx.component("forgero:diamond_hoe");

        assertTrue(hoe.isPresent(), "Diamond hoe must exist");

        // Validate exact vanilla Minecraft stats (Diamond Hoe: durability=1561, damage=1, speed=4.0)
        assertEquals(1561f, getAttributeValue(hoe.get(), "forgero:durability"),
                "Diamond hoe durability must be 1561 (vanilla value)");
        assertEquals(1f, getAttributeValue(hoe.get(), "forgero:attack_damage"),
                "Diamond hoe attack damage must be 1 (vanilla value - all hoes deal 1 damage)");
        assertEquals(4.0f, getAttributeValue(hoe.get(), "forgero:attack_speed"),
                "Diamond hoe attack speed must be 4.0 (vanilla value)");

        context.complete();
    }

    // ==================== Netherite Tool Tests (continued) ====================

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void netherite_axe_has_vanilla_stats(TestContext context) {
        var ctx = ForgeroTestUtils.forgero(context);
        var axe = ctx.component("forgero:netherite_axe");

        assertTrue(axe.isPresent(), "Netherite axe must exist");

        // Validate exact vanilla Minecraft stats (Netherite Axe: durability=2031, damage=10, speed=1.0)
        assertEquals(2031f, getAttributeValue(axe.get(), "forgero:durability"),
                "Netherite axe durability must be 2031 (vanilla value)");
        assertEquals(10f, getAttributeValue(axe.get(), "forgero:attack_damage"),
                "Netherite axe attack damage must be 10 (vanilla value)");
        assertEquals(1.0f, getAttributeValue(axe.get(), "forgero:attack_speed"),
                "Netherite axe attack speed must be 1.0 (vanilla value)");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void netherite_shovel_has_vanilla_stats(TestContext context) {
        var ctx = ForgeroTestUtils.forgero(context);
        var shovel = ctx.component("forgero:netherite_shovel");

        assertTrue(shovel.isPresent(), "Netherite shovel must exist");

        // Validate exact vanilla Minecraft stats (Netherite Shovel: durability=2031, damage=6.5, speed=1.0)
        assertEquals(2031f, getAttributeValue(shovel.get(), "forgero:durability"),
                "Netherite shovel durability must be 2031 (vanilla value)");
        assertEquals(6.5f, getAttributeValue(shovel.get(), "forgero:attack_damage"),
                "Netherite shovel attack damage must be 6.5 (vanilla value)");
        assertEquals(1.0f, getAttributeValue(shovel.get(), "forgero:attack_speed"),
                "Netherite shovel attack speed must be 1.0 (vanilla value)");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void netherite_hoe_has_vanilla_stats(TestContext context) {
        var ctx = ForgeroTestUtils.forgero(context);
        var hoe = ctx.component("forgero:netherite_hoe");

        assertTrue(hoe.isPresent(), "Netherite hoe must exist");

        // Validate exact vanilla Minecraft stats (Netherite Hoe: durability=2031, damage=1, speed=4.0)
        assertEquals(2031f, getAttributeValue(hoe.get(), "forgero:durability"),
                "Netherite hoe durability must be 2031 (vanilla value)");
        assertEquals(1f, getAttributeValue(hoe.get(), "forgero:attack_damage"),
                "Netherite hoe attack damage must be 1 (vanilla value - all hoes deal 1 damage)");
        assertEquals(4.0f, getAttributeValue(hoe.get(), "forgero:attack_speed"),
                "Netherite hoe attack speed must be 4.0 (vanilla value)");

        context.complete();
    }

    // ==================== Gold Tool Tests (continued) ====================

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void gold_axe_has_vanilla_stats(TestContext context) {
        var ctx = ForgeroTestUtils.forgero(context);
        var axe = ctx.component("forgero:golden_axe");

        assertTrue(axe.isPresent(), "Gold axe must exist");

        // Validate exact vanilla Minecraft stats (Gold Axe: durability=32, damage=7, speed=1.0)
        assertEquals(32f, getAttributeValue(axe.get(), "forgero:durability"),
                "Gold axe durability must be 32 (vanilla value)");
        assertEquals(7f, getAttributeValue(axe.get(), "forgero:attack_damage"),
                "Gold axe attack damage must be 7 (vanilla value)");
        assertEquals(1.0f, getAttributeValue(axe.get(), "forgero:attack_speed"),
                "Gold axe attack speed must be 1.0 (vanilla value)");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void gold_shovel_has_vanilla_stats(TestContext context) {
        var ctx = ForgeroTestUtils.forgero(context);
        var shovel = ctx.component("forgero:golden_shovel");

        assertTrue(shovel.isPresent(), "Gold shovel must exist");

        // Validate exact vanilla Minecraft stats (Gold Shovel: durability=32, damage=2.5, speed=1.0)
        assertEquals(32f, getAttributeValue(shovel.get(), "forgero:durability"),
                "Gold shovel durability must be 32 (vanilla value)");
        assertEquals(2.5f, getAttributeValue(shovel.get(), "forgero:attack_damage"),
                "Gold shovel attack damage must be 2.5 (vanilla value)");
        assertEquals(1.0f, getAttributeValue(shovel.get(), "forgero:attack_speed"),
                "Gold shovel attack speed must be 1.0 (vanilla value)");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void gold_hoe_has_vanilla_stats(TestContext context) {
        var ctx = ForgeroTestUtils.forgero(context);
        var hoe = ctx.component("forgero:golden_hoe");

        assertTrue(hoe.isPresent(), "Gold hoe must exist");

        // Validate exact vanilla Minecraft stats (Gold Hoe: durability=32, damage=1, speed=1.0)
        assertEquals(32f, getAttributeValue(hoe.get(), "forgero:durability"),
                "Gold hoe durability must be 32 (vanilla value)");
        assertEquals(1f, getAttributeValue(hoe.get(), "forgero:attack_damage"),
                "Gold hoe attack damage must be 1 (vanilla value - all hoes deal 1 damage)");
        assertEquals(1.0f, getAttributeValue(hoe.get(), "forgero:attack_speed"),
                "Gold hoe attack speed must be 1.0 (vanilla value)");

        context.complete();
    }

    // ==================== Wood Tool Tests ====================

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void wood_pickaxe_has_vanilla_stats(TestContext context) {
        var ctx = ForgeroTestUtils.forgero(context);
        var pickaxe = ctx.component("forgero:wooden_pickaxe");

        assertTrue(pickaxe.isPresent(), "Wood pickaxe must exist");

        // Validate exact vanilla Minecraft stats (Wood Pickaxe: durability=59, damage=2, speed=2, level=0)
        assertEquals(59f, getAttributeValue(pickaxe.get(), "forgero:durability"),
                "Wood pickaxe durability must be 59 (vanilla value)");
        assertEquals(2f, getAttributeValue(pickaxe.get(), "forgero:attack_damage"),
                "Wood pickaxe attack damage must be 2 (vanilla value)");
        assertEquals(2f, getAttributeValue(pickaxe.get(), "forgero:mining_speed"),
                "Wood pickaxe mining speed must be 2 (vanilla value)");
        assertEquals(0f, getAttributeValue(pickaxe.get(), "forgero:mining_level"),
                "Wood pickaxe mining level must be 0 (vanilla value)");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void wood_sword_has_vanilla_stats(TestContext context) {
        var ctx = ForgeroTestUtils.forgero(context);
        var sword = ctx.component("forgero:wooden_sword");

        assertTrue(sword.isPresent(), "Wood sword must exist");

        // Validate exact vanilla Minecraft stats (Wood Sword: durability=59, damage=4, speed=1.6)
        assertEquals(59f, getAttributeValue(sword.get(), "forgero:durability"),
                "Wood sword durability must be 59 (vanilla value)");
        assertEquals(4f, getAttributeValue(sword.get(), "forgero:attack_damage"),
                "Wood sword attack damage must be 4 (vanilla value)");
        assertEquals(1.6f, getAttributeValue(sword.get(), "forgero:attack_speed"),
                "Wood sword attack speed must be 1.6 (vanilla value)");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void wood_axe_has_vanilla_stats(TestContext context) {
        var ctx = ForgeroTestUtils.forgero(context);
        var axe = ctx.component("forgero:wooden_axe");

        assertTrue(axe.isPresent(), "Wood axe must exist");

        // Validate exact vanilla Minecraft stats (Wood Axe: durability=59, damage=7, speed=0.8)
        assertEquals(59f, getAttributeValue(axe.get(), "forgero:durability"),
                "Wood axe durability must be 59 (vanilla value)");
        assertEquals(7f, getAttributeValue(axe.get(), "forgero:attack_damage"),
                "Wood axe attack damage must be 7 (vanilla value)");
        assertEquals(0.8f, getAttributeValue(axe.get(), "forgero:attack_speed"),
                "Wood axe attack speed must be 0.8 (vanilla value)");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void wood_shovel_has_vanilla_stats(TestContext context) {
        var ctx = ForgeroTestUtils.forgero(context);
        var shovel = ctx.component("forgero:wooden_shovel");

        assertTrue(shovel.isPresent(), "Wood shovel must exist");

        // Validate exact vanilla Minecraft stats (Wood Shovel: durability=59, damage=2.5, speed=1.0)
        assertEquals(59f, getAttributeValue(shovel.get(), "forgero:durability"),
                "Wood shovel durability must be 59 (vanilla value)");
        assertEquals(2.5f, getAttributeValue(shovel.get(), "forgero:attack_damage"),
                "Wood shovel attack damage must be 2.5 (vanilla value)");
        assertEquals(1.0f, getAttributeValue(shovel.get(), "forgero:attack_speed"),
                "Wood shovel attack speed must be 1.0 (vanilla value)");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void wood_hoe_has_vanilla_stats(TestContext context) {
        var ctx = ForgeroTestUtils.forgero(context);
        var hoe = ctx.component("forgero:wooden_hoe");

        assertTrue(hoe.isPresent(), "Wood hoe must exist");

        // Validate exact vanilla Minecraft stats (Wood Hoe: durability=59, damage=1, speed=1.0)
        assertEquals(59f, getAttributeValue(hoe.get(), "forgero:durability"),
                "Wood hoe durability must be 59 (vanilla value)");
        assertEquals(1f, getAttributeValue(hoe.get(), "forgero:attack_damage"),
                "Wood hoe attack damage must be 1 (vanilla value - all hoes deal 1 damage)");
        assertEquals(1.0f, getAttributeValue(hoe.get(), "forgero:attack_speed"),
                "Wood hoe attack speed must be 1.0 (vanilla value)");

        context.complete();
    }

    // ==================== Stone Tool Tests ====================

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void stone_pickaxe_has_vanilla_stats(TestContext context) {
        var ctx = ForgeroTestUtils.forgero(context);
        var pickaxe = ctx.component("forgero:stone_pickaxe");

        assertTrue(pickaxe.isPresent(), "Stone pickaxe must exist");

        // Validate exact vanilla Minecraft stats (Stone Pickaxe: durability=131, damage=3, speed=4, level=1)
        assertEquals(131f, getAttributeValue(pickaxe.get(), "forgero:durability"),
                "Stone pickaxe durability must be 131 (vanilla value)");
        assertEquals(3f, getAttributeValue(pickaxe.get(), "forgero:attack_damage"),
                "Stone pickaxe attack damage must be 3 (vanilla value)");
        assertEquals(4f, getAttributeValue(pickaxe.get(), "forgero:mining_speed"),
                "Stone pickaxe mining speed must be 4 (vanilla value)");
        assertEquals(1f, getAttributeValue(pickaxe.get(), "forgero:mining_level"),
                "Stone pickaxe mining level must be 1 (vanilla value)");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void stone_sword_has_vanilla_stats(TestContext context) {
        var ctx = ForgeroTestUtils.forgero(context);
        var sword = ctx.component("forgero:stone_sword");

        assertTrue(sword.isPresent(), "Stone sword must exist");

        // Validate exact vanilla Minecraft stats (Stone Sword: durability=131, damage=5, speed=1.6)
        assertEquals(131f, getAttributeValue(sword.get(), "forgero:durability"),
                "Stone sword durability must be 131 (vanilla value)");
        assertEquals(5f, getAttributeValue(sword.get(), "forgero:attack_damage"),
                "Stone sword attack damage must be 5 (vanilla value)");
        assertEquals(1.6f, getAttributeValue(sword.get(), "forgero:attack_speed"),
                "Stone sword attack speed must be 1.6 (vanilla value)");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void stone_axe_has_vanilla_stats(TestContext context) {
        var ctx = ForgeroTestUtils.forgero(context);
        var axe = ctx.component("forgero:stone_axe");

        assertTrue(axe.isPresent(), "Stone axe must exist");

        // Validate exact vanilla Minecraft stats (Stone Axe: durability=131, damage=9, speed=0.8)
        assertEquals(131f, getAttributeValue(axe.get(), "forgero:durability"),
                "Stone axe durability must be 131 (vanilla value)");
        assertEquals(9f, getAttributeValue(axe.get(), "forgero:attack_damage"),
                "Stone axe attack damage must be 9 (vanilla value)");
        assertEquals(0.8f, getAttributeValue(axe.get(), "forgero:attack_speed"),
                "Stone axe attack speed must be 0.8 (vanilla value)");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void stone_shovel_has_vanilla_stats(TestContext context) {
        var ctx = ForgeroTestUtils.forgero(context);
        var shovel = ctx.component("forgero:stone_shovel");

        assertTrue(shovel.isPresent(), "Stone shovel must exist");

        // Validate exact vanilla Minecraft stats (Stone Shovel: durability=131, damage=3.5, speed=1.0)
        assertEquals(131f, getAttributeValue(shovel.get(), "forgero:durability"),
                "Stone shovel durability must be 131 (vanilla value)");
        assertEquals(3.5f, getAttributeValue(shovel.get(), "forgero:attack_damage"),
                "Stone shovel attack damage must be 3.5 (vanilla value)");
        assertEquals(1.0f, getAttributeValue(shovel.get(), "forgero:attack_speed"),
                "Stone shovel attack speed must be 1.0 (vanilla value)");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void stone_hoe_has_vanilla_stats(TestContext context) {
        var ctx = ForgeroTestUtils.forgero(context);
        var hoe = ctx.component("forgero:stone_hoe");

        assertTrue(hoe.isPresent(), "Stone hoe must exist");

        // Validate exact vanilla Minecraft stats (Stone Hoe: durability=131, damage=1, speed=2.0)
        assertEquals(131f, getAttributeValue(hoe.get(), "forgero:durability"),
                "Stone hoe durability must be 131 (vanilla value)");
        assertEquals(1f, getAttributeValue(hoe.get(), "forgero:attack_damage"),
                "Stone hoe attack damage must be 1 (vanilla value - all hoes deal 1 damage)");
        assertEquals(2.0f, getAttributeValue(hoe.get(), "forgero:attack_speed"),
                "Stone hoe attack speed must be 2.0 (vanilla value)");

        context.complete();
    }

    // ==================== Hoe Consistency Tests ====================

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void all_hoes_deal_exactly_1_damage(TestContext context) {
        // This test validates that ALL hoes deal exactly 1 damage, regardless of material tier
        // This is a core vanilla Minecraft mechanic that must be preserved
        var ctx = ForgeroTestUtils.forgero(context);

        // Test all material tiers
        String[] materials = {"wooden", "stone", "iron", "golden", "diamond", "netherite"};

        for (String material : materials) {
            var hoe = ctx.component("forgero:" + material + "_hoe");
            assertTrue(hoe.isPresent(), material + " hoe must exist");

            float damage = getAttributeValue(hoe.get(), "forgero:attack_damage");
            assertEquals(1f, damage,
                    material + " hoe attack damage must be 1 (vanilla rule: all hoes deal 1 damage)");
        }

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void hoe_attack_speed_tiers_are_correct(TestContext context) {
        // Vanilla hoe attack speeds: wood/gold=1.0, stone=2.0, iron=3.0, diamond/netherite=4.0
        var ctx = ForgeroTestUtils.forgero(context);

        // Wood and Gold have speed 1.0
        assertEquals(1.0f, getAttributeValue(ctx.component("forgero:wooden_hoe").orElseThrow(), "forgero:attack_speed"),
                "Wood hoe attack speed must be 1.0");
        assertEquals(1.0f, getAttributeValue(ctx.component("forgero:golden_hoe").orElseThrow(), "forgero:attack_speed"),
                "Gold hoe attack speed must be 1.0");

        // Stone has speed 2.0
        assertEquals(2.0f, getAttributeValue(ctx.component("forgero:stone_hoe").orElseThrow(), "forgero:attack_speed"),
                "Stone hoe attack speed must be 2.0");

        // Iron has speed 3.0
        assertEquals(3.0f, getAttributeValue(ctx.component("forgero:iron_hoe").orElseThrow(), "forgero:attack_speed"),
                "Iron hoe attack speed must be 3.0");

        // Diamond and Netherite have speed 4.0
        assertEquals(4.0f, getAttributeValue(ctx.component("forgero:diamond_hoe").orElseThrow(), "forgero:attack_speed"),
                "Diamond hoe attack speed must be 4.0");
        assertEquals(4.0f, getAttributeValue(ctx.component("forgero:netherite_hoe").orElseThrow(), "forgero:attack_speed"),
                "Netherite hoe attack speed must be 4.0");

        context.complete();
    }

    // ==================== Axe Consistency Tests ====================

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void axe_attack_damage_tiers_are_correct(TestContext context) {
        // Vanilla axe damages: wood=7, stone=9, iron=9, gold=7, diamond=9, netherite=10
        var ctx = ForgeroTestUtils.forgero(context);

        // Wood and Gold have damage 7
        assertEquals(7f, getAttributeValue(ctx.component("forgero:wooden_axe").orElseThrow(), "forgero:attack_damage"),
                "Wood axe attack damage must be 7");
        assertEquals(7f, getAttributeValue(ctx.component("forgero:golden_axe").orElseThrow(), "forgero:attack_damage"),
                "Gold axe attack damage must be 7");

        // Stone, Iron, Diamond have damage 9
        assertEquals(9f, getAttributeValue(ctx.component("forgero:stone_axe").orElseThrow(), "forgero:attack_damage"),
                "Stone axe attack damage must be 9");
        assertEquals(9f, getAttributeValue(ctx.component("forgero:iron_axe").orElseThrow(), "forgero:attack_damage"),
                "Iron axe attack damage must be 9");
        assertEquals(9f, getAttributeValue(ctx.component("forgero:diamond_axe").orElseThrow(), "forgero:attack_damage"),
                "Diamond axe attack damage must be 9");

        // Netherite has damage 10
        assertEquals(10f, getAttributeValue(ctx.component("forgero:netherite_axe").orElseThrow(), "forgero:attack_damage"),
                "Netherite axe attack damage must be 10");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void axe_attack_speed_tiers_are_correct(TestContext context) {
        // Vanilla axe speeds: wood/stone=0.8, iron=0.9, gold/diamond/netherite=1.0
        var ctx = ForgeroTestUtils.forgero(context);

        // Wood and Stone have speed 0.8
        assertEquals(0.8f, getAttributeValue(ctx.component("forgero:wooden_axe").orElseThrow(), "forgero:attack_speed"),
                "Wood axe attack speed must be 0.8");
        assertEquals(0.8f, getAttributeValue(ctx.component("forgero:stone_axe").orElseThrow(), "forgero:attack_speed"),
                "Stone axe attack speed must be 0.8");

        // Iron has speed 0.9
        assertEquals(0.9f, getAttributeValue(ctx.component("forgero:iron_axe").orElseThrow(), "forgero:attack_speed"),
                "Iron axe attack speed must be 0.9");

        // Gold, Diamond, Netherite have speed 1.0
        assertEquals(1.0f, getAttributeValue(ctx.component("forgero:golden_axe").orElseThrow(), "forgero:attack_speed"),
                "Gold axe attack speed must be 1.0");
        assertEquals(1.0f, getAttributeValue(ctx.component("forgero:diamond_axe").orElseThrow(), "forgero:attack_speed"),
                "Diamond axe attack speed must be 1.0");
        assertEquals(1.0f, getAttributeValue(ctx.component("forgero:netherite_axe").orElseThrow(), "forgero:attack_speed"),
                "Netherite axe attack speed must be 1.0");

        context.complete();
    }

    // ==================== Material Tier Ordering Tests ====================

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void tool_tiers_ordered_correctly_by_durability(TestContext context) {
        // Load all pickaxes
        var gold = ForgeroTestUtils.forgero(context).component("forgero:golden_pickaxe").orElseThrow();
        var iron = ForgeroTestUtils.forgero(context).component("forgero:iron_pickaxe").orElseThrow();
        var diamond = ForgeroTestUtils.forgero(context).component("forgero:diamond_pickaxe").orElseThrow();
        var netherite = ForgeroTestUtils.forgero(context).component("forgero:netherite_pickaxe").orElseThrow();

        float goldDur = getAttributeValue(gold, "forgero:durability");
        float ironDur = getAttributeValue(iron, "forgero:durability");
        float diamondDur = getAttributeValue(diamond, "forgero:durability");
        float netheriteDur = getAttributeValue(netherite, "forgero:durability");

        // Validate correct ordering: gold < iron < diamond < netherite
        assertTrue(goldDur < ironDur, "Gold durability (" + goldDur + ") must be less than Iron (" + ironDur + ")");
        assertTrue(ironDur < diamondDur, "Iron durability (" + ironDur + ") must be less than Diamond (" + diamondDur + ")");
        assertTrue(diamondDur < netheriteDur, "Diamond durability (" + diamondDur + ") must be less than Netherite (" + netheriteDur + ")");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void tool_tiers_ordered_correctly_by_damage(TestContext context) {
        // Load all swords
        var gold = ForgeroTestUtils.forgero(context).component("forgero:golden_sword").orElseThrow();
        var iron = ForgeroTestUtils.forgero(context).component("forgero:iron_sword").orElseThrow();
        var diamond = ForgeroTestUtils.forgero(context).component("forgero:diamond_sword").orElseThrow();
        var netherite = ForgeroTestUtils.forgero(context).component("forgero:netherite_sword").orElseThrow();

        float goldDmg = getAttributeValue(gold, "forgero:attack_damage");
        float ironDmg = getAttributeValue(iron, "forgero:attack_damage");
        float diamondDmg = getAttributeValue(diamond, "forgero:attack_damage");
        float netheriteDmg = getAttributeValue(netherite, "forgero:attack_damage");

        // Validate correct ordering: gold < iron < diamond < netherite
        assertTrue(goldDmg < ironDmg, "Gold damage (" + goldDmg + ") must be less than Iron (" + ironDmg + ")");
        assertTrue(ironDmg < diamondDmg, "Iron damage (" + ironDmg + ") must be less than Diamond (" + diamondDmg + ")");
        assertTrue(diamondDmg < netheriteDmg, "Diamond damage (" + diamondDmg + ") must be less than Netherite (" + netheriteDmg + ")");

        context.complete();
    }

    // ==================== Mixin Path Verification Tests ====================

    /**
     * Comprehensive test that dumps ALL attribute modifiers from ItemStack.getAttributeModifiers()
     * for multiple tool types to verify actual in-game values.
     */
    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void dump_all_attribute_modifiers(TestContext context) {
        var ctx = ForgeroTestUtils.forgero(context);

        String[] tools = {
            "forgero:golden_sword", "forgero:iron_sword", "forgero:diamond_sword",
            "forgero:golden_axe", "forgero:iron_axe", "forgero:diamond_axe",
            "forgero:golden_pickaxe", "forgero:iron_pickaxe",
            "forgero:golden_hoe", "forgero:iron_hoe", "forgero:diamond_hoe"
        };

        System.out.println("\n========== ATTRIBUTE MODIFIER DUMP ==========");

        for (String toolId : tools) {
            var component = ctx.component(toolId);
            if (component.isEmpty()) {
                System.out.println("[WARN] Component not found: " + toolId);
                continue;
            }

            ItemStack stack = ForgeroApi.converter().toStack(component.get()).orElse(null);
            if (stack == null || stack.isEmpty()) {
                System.out.println("[WARN] Failed to convert to ItemStack: " + toolId);
                continue;
            }

            System.out.println("\n--- " + toolId + " ---");
            System.out.println("  Item: " + stack.getItem().getClass().getSimpleName());

            var modifiers = stack.getAttributeModifiers(net.minecraft.entity.EquipmentSlot.MAINHAND);

            // Attack Damage
            var damageModifiers = modifiers.get(net.minecraft.entity.attribute.EntityAttributes.GENERIC_ATTACK_DAMAGE);
            float totalDamageMod = 0f;
            for (var mod : damageModifiers) {
                System.out.println("  ATTACK_DAMAGE modifier: " + mod.getName() + " = " + mod.getValue() + " (" + mod.getOperation() + ")");
                if (mod.getOperation() == net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADDITION) {
                    totalDamageMod += mod.getValue();
                }
            }
            System.out.println("  -> Final Attack Damage: " + (1.0 + totalDamageMod) + " (base 1.0 + " + totalDamageMod + ")");

            // Attack Speed
            var speedModifiers = modifiers.get(net.minecraft.entity.attribute.EntityAttributes.GENERIC_ATTACK_SPEED);
            float totalSpeedMod = 0f;
            for (var mod : speedModifiers) {
                System.out.println("  ATTACK_SPEED modifier: " + mod.getName() + " = " + mod.getValue() + " (" + mod.getOperation() + ")");
                if (mod.getOperation() == net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADDITION) {
                    totalSpeedMod += mod.getValue();
                }
            }
            System.out.println("  -> Final Attack Speed: " + (4.0 + totalSpeedMod) + " (base 4.0 + " + totalSpeedMod + ")");

            // Compare with ItemQueryApi
            float queryDamage = ForgeroApi.itemQuery().getAttackDamage(stack);
            float querySpeed = ForgeroApi.itemQuery().getAttackSpeed(stack);
            System.out.println("  ItemQueryApi - damage: " + queryDamage + ", speed: " + querySpeed);
        }

        System.out.println("\n========== END DUMP ==========\n");

        // Now run actual assertions for gold sword
        var goldSword = ctx.component("forgero:golden_sword").orElseThrow();
        ItemStack goldStack = ForgeroApi.converter().toStack(goldSword).orElseThrow();

        var goldModifiers = goldStack.getAttributeModifiers(net.minecraft.entity.EquipmentSlot.MAINHAND);
        var goldSpeedMods = goldModifiers.get(net.minecraft.entity.attribute.EntityAttributes.GENERIC_ATTACK_SPEED);

        float goldSpeedModValue = 0f;
        for (var mod : goldSpeedMods) {
            goldSpeedModValue += (float) mod.getValue();
        }
        float goldFinalSpeed = 4.0f + goldSpeedModValue;

        // Gold sword should have modifier -2.4, final speed 1.6
        assertEquals(-2.4f, goldSpeedModValue, 0.01f,
                "Gold sword attack speed MODIFIER must be -2.4 (vanilla: 4.0 base + -2.4 = 1.6 speed)");
        assertEquals(1.6f, goldFinalSpeed, 0.01f,
                "Gold sword final attack speed must be 1.6");

        context.complete();
    }

    /**
     * Diagnostic test to trace exactly what's happening during attribute composition
     * for iron vs gold pickaxes to find why iron returns 0 for attack_speed.
     */
    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void trace_pickaxe_composition(TestContext context) {
        var ctx = ForgeroTestUtils.forgero(context);

        System.out.println("\n========== PICKAXE COMPOSITION TRACE ==========");

        for (String material : new String[]{"golden", "iron"}) {
            String pickaxeId = "forgero:" + material + "_pickaxe";
            var pickaxeOpt = ctx.component(pickaxeId);

            if (pickaxeOpt.isEmpty()) {
                System.out.println("[ERROR] " + pickaxeId + " not found!");
                continue;
            }

            System.out.println("\n--- " + pickaxeId + " ---");
            var pickaxe = pickaxeOpt.get();
            System.out.println("  Component type: " + pickaxe.getClass().getSimpleName());
            System.out.println("  Interfaces: " + java.util.Arrays.toString(pickaxe.getClass().getInterfaces()));

            // Direct attributes on pickaxe
            var pickaxeAttrs = pickaxe.properties(com.sigmundgranaas.forgero.core.attribute.api.Attribute.KEY);
            System.out.println("  Direct attributes (" + pickaxeAttrs.size() + "):");
            for (var attr : pickaxeAttrs) {
                if (attr.type().path().contains("attack")) {
                    System.out.println("    * " + attr.type() + " = " + attr.value() +
                        " op=" + attr.operator().getClass().getSimpleName() +
                        " scope=" + attr.scope().orElse(null));
                }
            }

            // Print pickaxe's structure
            if (pickaxe instanceof com.sigmundgranaas.forgero.core.component.api.StructuredComponent structured) {
                System.out.println("  IS StructuredComponent - parts:");
                for (var part : structured.structure().allParts()) {
                    System.out.println("    - slot=" + part.id() + " content=" + part.getContent().id());
                    dumpComponentAttrsRecursive(part.getContent(), "      ", "attack");
                }
            } else {
                System.out.println("  NOT a StructuredComponent");
            }

            // Get resolved value via ItemQueryApi
            ItemStack stack = ForgeroApi.converter().toStack(pickaxe).orElseThrow();
            float querySpeed = ForgeroApi.itemQuery().getAttackSpeed(stack);
            float queryDamage = ForgeroApi.itemQuery().getAttackDamage(stack);
            System.out.println("  RESOLVED via ItemQueryApi: attack_speed=" + querySpeed + ", attack_damage=" + queryDamage);
        }

        System.out.println("\n========== END TRACE ==========\n");

        context.complete();
    }

    private void dumpComponentAttrsRecursive(com.sigmundgranaas.forgero.core.component.api.Component comp, String indent, String filter) {
        var attrs = comp.properties(com.sigmundgranaas.forgero.core.attribute.api.Attribute.KEY);
        int matchCount = 0;
        for (var attr : attrs) {
            if (attr.type().path().contains(filter)) {
                matchCount++;
                System.out.println(indent + "* " + attr.type() + " = " + attr.value() +
                    " op=" + attr.operator().getClass().getSimpleName() +
                    " order=" + attr.operator().order() +
                    " scope=" + attr.scope().orElse(null) +
                    " cond=" + attr.condition().map(c -> c.staticConditions().size() + " static").orElse("none"));
            }
        }
        System.out.println(indent + "(found " + matchCount + " matching attrs out of " + attrs.size() + " total)");

        if (comp instanceof com.sigmundgranaas.forgero.core.component.api.StructuredComponent structured) {
            for (var part : structured.structure().allParts()) {
                System.out.println(indent + "-> slot=" + part.id() + " content=" + part.getContent().id());
                dumpComponentAttrsRecursive(part.getContent(), indent + "  ", filter);
            }
        }
    }
}
