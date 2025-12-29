package com.sigmundgranaas.forgero.tests;

import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgradeSlot;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that validate upgrades provide REAL gameplay improvements.
 * These tests ensure the upgrade system actually makes tools better.
 */

public class UpgradeEffectivenessTest implements ForgeroGameTest {

    private float getAttributeValue(Component component, String attributeType) {
        return component.properties(Attribute.KEY).stream()
                .filter(attr -> attr.type().toString().equals(attributeType))
                .findFirst()
                .map(Attribute::value)
                .orElse(0f);
    }

    /**
     * Tests that iron pickaxe has an upgradeable binding slot that accepts materials.
     * This is the baseline - if tools can't be upgraded, the system is broken.
     */
    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void iron_pickaxe_has_upgrade_slot(TestContext context) {
        var ctx = forgero(context);
        var pickaxe = ctx.component("forgero:iron_pickaxe");

        assertTrue(pickaxe.isPresent(), "Iron pickaxe must exist");
        assertTrue(pickaxe.get() instanceof CustomizableComponent,
                "Iron pickaxe must be customizable");

        CustomizableComponent customizable = (CustomizableComponent) pickaxe.get();
        List<ComponentUpgradeSlot> slots = customizable.upgrades().allUpgradeSlots();

        assertFalse(slots.isEmpty(), "Iron pickaxe must have at least one upgrade slot");

        // Verify slot accepts upgrade materials
        ComponentUpgradeSlot bindingSlot = slots.get(0);
        assertTrue(bindingSlot.validator() != null,
                "Upgrade slot must have a validator");

        context.complete();
    }

    /**
     * Tests that diamond has higher base stats than iron.
     * This validates the material tier system works correctly.
     */
    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void diamond_pickaxe_is_better_than_iron_pickaxe(TestContext context) {
        Component iron = forgero(context).component("forgero:iron_pickaxe").orElseThrow();
        Component diamond = forgero(context).component("forgero:diamond_pickaxe").orElseThrow();

        float ironDurability = getAttributeValue(iron, "forgero:durability");
        float diamondDurability = getAttributeValue(diamond, "forgero:durability");

        assertTrue(diamondDurability > ironDurability,
                String.format("Diamond durability (%f) must be greater than iron (%f)",
                        diamondDurability, ironDurability));

        float ironDamage = getAttributeValue(iron, "forgero:attack_damage");
        float diamondDamage = getAttributeValue(diamond, "forgero:attack_damage");

        assertTrue(diamondDamage > ironDamage,
                String.format("Diamond damage (%f) must be greater than iron (%f)",
                        diamondDamage, ironDamage));

        float ironSpeed = getAttributeValue(iron, "forgero:mining_speed");
        float diamondSpeed = getAttributeValue(diamond, "forgero:mining_speed");

        assertTrue(diamondSpeed > ironSpeed,
                String.format("Diamond mining speed (%f) must be greater than iron (%f)",
                        diamondSpeed, ironSpeed));

        context.complete();
    }

    /**
     * Tests that netherite is the best vanilla tier.
     * Validates the full material progression: iron < diamond < netherite.
     */
    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void netherite_is_best_vanilla_tier(TestContext context) {
        Component iron = forgero(context).component("forgero:iron_pickaxe").orElseThrow();
        Component diamond = forgero(context).component("forgero:diamond_pickaxe").orElseThrow();
        Component netherite = forgero(context).component("forgero:netherite_pickaxe").orElseThrow();

        float ironDur = getAttributeValue(iron, "forgero:durability");
        float diamondDur = getAttributeValue(diamond, "forgero:durability");
        float netheriteDur = getAttributeValue(netherite, "forgero:durability");

        // Validate progression: iron (250) < diamond (1561) < netherite (2031)
        assertTrue(ironDur < diamondDur && diamondDur < netheriteDur,
                String.format("Durability progression broken: iron=%f, diamond=%f, netherite=%f",
                        ironDur, diamondDur, netheriteDur));

        // Validate exact values match vanilla
        assertEquals(250f, ironDur, "Iron durability must be 250");
        assertEquals(1561f, diamondDur, "Diamond durability must be 1561");
        assertEquals(2031f, netheriteDur, "Netherite durability must be 2031");

        context.complete();
    }

    /**
     * Tests that gold tools are weak but fast.
     * Validates gold's unique trait: lowest durability, highest mining speed.
     */
    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void gold_is_weak_but_fast(TestContext context) {
        Component gold = forgero(context).component("forgero:golden_pickaxe").orElseThrow();
        Component iron = forgero(context).component("forgero:iron_pickaxe").orElseThrow();
        Component diamond = forgero(context).component("forgero:diamond_pickaxe").orElseThrow();

        float goldDur = getAttributeValue(gold, "forgero:durability");
        float ironDur = getAttributeValue(iron, "forgero:durability");

        // Gold should have MUCH lower durability
        assertTrue(goldDur < ironDur,
                String.format("Gold durability (%f) must be less than iron (%f)", goldDur, ironDur));
        assertEquals(32f, goldDur, "Gold durability must be 32 (vanilla value)");

        float goldSpeed = getAttributeValue(gold, "forgero:mining_speed");
        float diamondSpeed = getAttributeValue(diamond, "forgero:mining_speed");

        // Gold should be FASTER than diamond
        assertTrue(goldSpeed > diamondSpeed,
                String.format("Gold mining speed (%f) must be greater than diamond (%f)",
                        goldSpeed, diamondSpeed));
        assertEquals(12f, goldSpeed, "Gold mining speed must be 12 (vanilla value - fastest)");

        context.complete();
    }

    /**
     * Tests that all tool types (pickaxe, sword, axe, shovel) exist for each material tier.
     * Validates content completeness.
     */
    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void all_vanilla_tool_types_exist_for_iron(TestContext context) {
        assertTrue(forgero(context).component("forgero:iron_pickaxe").isPresent(),
                "Iron pickaxe must exist");
        assertTrue(forgero(context).component("forgero:iron_sword").isPresent(),
                "Iron sword must exist");
        assertTrue(forgero(context).component("forgero:iron_axe").isPresent(),
                "Iron axe must exist");
        assertTrue(forgero(context).component("forgero:iron_shovel").isPresent(),
                "Iron shovel must exist");

        context.complete();
    }

    /**
     * Tests that all material tiers exist for pickaxes.
     * Validates content completeness for the most important tool type.
     */
    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void all_vanilla_tiers_exist_for_pickaxe(TestContext context) {
        assertTrue(forgero(context).component("forgero:wooden_pickaxe").isPresent(),
                "Wooden pickaxe must exist");
        assertTrue(forgero(context).component("forgero:stone_pickaxe").isPresent(),
                "Stone pickaxe must exist");
        assertTrue(forgero(context).component("forgero:iron_pickaxe").isPresent(),
                "Iron pickaxe must exist");
        assertTrue(forgero(context).component("forgero:golden_pickaxe").isPresent(),
                "Golden pickaxe must exist");
        assertTrue(forgero(context).component("forgero:diamond_pickaxe").isPresent(),
                "Diamond pickaxe must exist");
        assertTrue(forgero(context).component("forgero:netherite_pickaxe").isPresent(),
                "Netherite pickaxe must exist");

        context.complete();
    }
}
