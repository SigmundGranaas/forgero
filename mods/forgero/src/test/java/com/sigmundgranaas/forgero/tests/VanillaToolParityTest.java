package com.sigmundgranaas.forgero.tests;

import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
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
 */
public class VanillaToolParityTest implements ForgeroGameTest {

    private float getAttributeValue(Component component, String attributeType) {
        return component.properties(Attribute.KEY).stream()
                .filter(attr -> attr.type().toString().equals(attributeType))
                .findFirst()
                .map(Attribute::value)
                .orElseThrow(() -> new AssertionError("Missing attribute: " + attributeType + " on " + component.id()));
    }

    // ==================== Iron Tool Tests ====================

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void iron_pickaxe_has_vanilla_stats(TestContext context) {
        var ctx = forgero(context);
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
        var ctx = forgero(context);
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
        var ctx = forgero(context);
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
        var ctx = forgero(context);
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
        var ctx = forgero(context);
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
        var ctx = forgero(context);
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
        var ctx = forgero(context);
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
        var ctx = forgero(context);
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
        var ctx = forgero(context);
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
        var ctx = forgero(context);
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
        var ctx = forgero(context);
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

    // ==================== Material Tier Ordering Tests ====================

    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void tool_tiers_ordered_correctly_by_durability(TestContext context) {
        // Load all pickaxes
        var gold = forgero(context).component("forgero:golden_pickaxe").orElseThrow();
        var iron = forgero(context).component("forgero:iron_pickaxe").orElseThrow();
        var diamond = forgero(context).component("forgero:diamond_pickaxe").orElseThrow();
        var netherite = forgero(context).component("forgero:netherite_pickaxe").orElseThrow();

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
        var gold = forgero(context).component("forgero:golden_sword").orElseThrow();
        var iron = forgero(context).component("forgero:iron_sword").orElseThrow();
        var diamond = forgero(context).component("forgero:diamond_sword").orElseThrow();
        var netherite = forgero(context).component("forgero:netherite_sword").orElseThrow();

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
}
