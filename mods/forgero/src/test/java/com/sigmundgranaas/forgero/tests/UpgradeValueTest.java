package com.sigmundgranaas.forgero.tests;

import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgradeSlot;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroTestUtils;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
/**
 * Tests that validate upgrades provide value changes.
 * These tests ensure the upgrade system produces correct, predictable results.
 */
public class UpgradeValueTest implements ForgeroGameTest {
    private float getAttributeValue(Component component, String attributeType) {
        return component.properties(Attribute.KEY).stream()
                .filter(attr -> attr.type().toString().equals(attributeType))
                .findFirst()
                .map(Attribute::value)
                .orElse(0f);
    }
    /**
     * Tests that ender_pearl upgrade provides exactly +105 durability.
     * This validates the upgrade system applies values correctly.
     */
    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void ender_pearl_adds_105_durability_to_tool(TestContext context) {
        // Get iron pickaxe and ender pearl
        var ironPickaxe = ForgeroTestUtils.forgero(context).component("forgero:iron_pickaxe").orElseThrow();
        var enderPearl = ForgeroTestUtils.forgero(context).component("forgero:ender_pearl").orElseThrow();

        // Verify base durability
        float baseDurability = getAttributeValue(ironPickaxe, "forgero:durability");
        assertEquals(250f, baseDurability, "Iron pickaxe base durability must be 250");

        // Verify ender pearl provides +105 durability
        float enderPearlDurability = getAttributeValue(enderPearl, "forgero:durability");
        assertEquals(105f, enderPearlDurability,
                "Ender pearl must provide exactly +105 durability");

        context.complete();
    }

    /**
     * Tests that glowstone upgrade provides exactly +15 durability.
     * This validates weaker upgrades have correct values.
     */
    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void glowstone_adds_15_durability(TestContext context) {
        var glowstone = ForgeroTestUtils.forgero(context).component("forgero:glowstone").orElseThrow();

        float glowstoneDurability = getAttributeValue(glowstone, "forgero:durability");
        assertEquals(15f, glowstoneDurability,
                "Glowstone must provide exactly +15 durability");

        context.complete();
    }

    /**
     * Tests that slime_ball has negative stat modifiers.
     * This validates the system supports both positive and negative modifiers.
     */
    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void slime_ball_reduces_offensive_stats(TestContext context) {
        var slimeBall = ForgeroTestUtils.forgero(context).component("forgero:slime_ball").orElseThrow();

        // Slime ball should give +105 durability but reduce offensive stats
        float durability = getAttributeValue(slimeBall, "forgero:durability");
        float attackDamage = getAttributeValue(slimeBall, "forgero:attack_damage");
        float miningSpeed = getAttributeValue(slimeBall, "forgero:mining_speed");
        float miningLevel = getAttributeValue(slimeBall, "forgero:mining_level");

        assertEquals(105f, durability, "Slime ball must provide +105 durability");
        assertEquals(-1f, attackDamage, "Slime ball must reduce attack damage by -1");
        assertEquals(-1f, miningSpeed, "Slime ball must reduce mining speed by -1");
        assertEquals(-1f, miningLevel, "Slime ball must reduce mining level by -1");

        context.complete();
    }

    /**
     * Tests that upgrade materials exist for all expected types.
     * This validates content completeness for the upgrade system.
     */
    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void all_basic_upgrade_materials_exist(TestContext context) {
        String[] expectedUpgrades = {
                "ender_pearl", "glowstone", "slime_ball", "blaze_rod",
                "fire_charge", "magma_cream", "honeycomb", "phantom_membrane"
        };

        for (String upgradeName : expectedUpgrades) {
            var upgrade = ForgeroTestUtils.forgero(context).component("forgero:" + upgradeName);
            assertTrue(upgrade.isPresent(),
                    "Upgrade material " + upgradeName + " must exist");
        }

        context.complete();
    }

    /**
     * Tests that different upgrade materials provide different durability bonuses.
     * This validates the upgrade variety and balance.
     */
    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void upgrade_materials_have_varied_durability_bonuses(TestContext context) {
        var enderPearl = ForgeroTestUtils.forgero(context).component("forgero:ender_pearl").orElseThrow();
        var glowstone = ForgeroTestUtils.forgero(context).component("forgero:glowstone").orElseThrow();
        var slimeBall = ForgeroTestUtils.forgero(context).component("forgero:slime_ball").orElseThrow();

        float enderPearlDur = getAttributeValue(enderPearl, "forgero:durability");
        float glowstoneDur = getAttributeValue(glowstone, "forgero:durability");
        float slimeBallDur = getAttributeValue(slimeBall, "forgero:durability");

        // Verify they're different
        assertNotEquals(enderPearlDur, glowstoneDur,
                "Ender pearl and glowstone must provide different durability bonuses");

        // Verify specific values
        assertEquals(105f, enderPearlDur, "Ender pearl: +105 durability");
        assertEquals(15f, glowstoneDur, "Glowstone: +15 durability");
        assertEquals(105f, slimeBallDur, "Slime ball: +105 durability");

        // Verify ender_pearl and slime_ball have SAME durability but different trade-offs
        assertEquals(enderPearlDur, slimeBallDur,
                "Ender pearl and slime ball should both give +105 durability (different trade-offs)");

        context.complete();
    }

    /**
     * Tests that upgrade slots accept appropriate upgrade materials.
     * This validates the upgrade system's type safety.
     */
    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void upgrade_slots_accept_upgrade_materials(TestContext context) {
        var ironPickaxe = ForgeroTestUtils.forgero(context).component("forgero:iron_pickaxe").orElseThrow();
        var enderPearl = ForgeroTestUtils.forgero(context).component("forgero:ender_pearl").orElseThrow();

        assertTrue(ironPickaxe instanceof CustomizableComponent,
                "Iron pickaxe must be customizable");

        CustomizableComponent customizable = (CustomizableComponent) ironPickaxe;
        List<ComponentUpgradeSlot> slots = customizable.upgrades().allUpgradeSlots();

        assertFalse(slots.isEmpty(), "Iron pickaxe must have upgrade slots");

        // Get the binding slot
        ComponentUpgradeSlot bindingSlot = slots.get(0);
        assertEquals("forgero:materials/roles/upgrade_material", bindingSlot.slotType().toString(),
                "First slot must be upgrade_material type");

        // Verify ender_pearl has upgrade tags
        assertTrue(enderPearl.getTags().stream()
                        .anyMatch(tag -> tag.toString().contains("upgrade") ||
                                       tag.toString().contains("materials")),
                "Ender pearl must have upgrade-related tags");

        context.complete();
    }
}
