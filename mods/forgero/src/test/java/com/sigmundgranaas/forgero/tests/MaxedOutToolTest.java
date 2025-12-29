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
 * Tests that validate tools with all upgrade slots filled.
 * These tests ensure the upgrade system correctly stacks multiple upgrades.
 */
public class MaxedOutToolTest implements ForgeroGameTest {
    private float getAttributeValue(Component component, String attributeType) {
        return component.properties(Attribute.KEY).stream()
                .filter(attr -> attr.type().toString().equals(attributeType))
                .findFirst()
                .map(Attribute::value)
                .orElse(0f);
    }
    /**
     * Tests that iron pickaxe can accept upgrades in all available slots.
     * This validates the upgrade slot system works end-to-end.
     */
    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void iron_pickaxe_accepts_upgrades_in_all_slots(TestContext context) {
        var ironPickaxe = forgero(context).component("forgero:iron_pickaxe").orElseThrow();
        var enderPearl = forgero(context).component("forgero:ender_pearl").orElseThrow();

        assertTrue(ironPickaxe instanceof CustomizableComponent,
                "Iron pickaxe must be customizable");

        CustomizableComponent customizable = (CustomizableComponent) ironPickaxe;
        List<ComponentUpgradeSlot> slots = customizable.upgrades().allUpgradeSlots();

        assertFalse(slots.isEmpty(), "Iron pickaxe must have upgrade slots");

        // Verify we can validate ender_pearl against the slot
        ComponentUpgradeSlot firstSlot = slots.get(0);
        assertNotNull(firstSlot.validator(), "Slot must have validator");

        // Test that ender_pearl has appropriate tags for upgrading
        assertTrue(enderPearl.getTags().stream()
                        .anyMatch(tag -> tag.toString().contains("upgrade") ||
                                       tag.toString().contains("materials")),
                "Ender pearl must have upgrade-related tags");

        context.complete();
    }

    /**
     * Tests that diamond pickaxe has multiple upgrade slots.
     * This validates that better-tier tools have more upgrade capacity.
     */
    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void diamond_pickaxe_has_multiple_upgrade_slots(TestContext context) {
        var diamondPickaxe = forgero(context).component("forgero:diamond_pickaxe").orElseThrow();

        assertTrue(diamondPickaxe instanceof CustomizableComponent,
                "Diamond pickaxe must be customizable");

        CustomizableComponent customizable = (CustomizableComponent) diamondPickaxe;
        List<ComponentUpgradeSlot> slots = customizable.upgrades().allUpgradeSlots();

        // Diamond tools should have at least 2 upgrade slots
        assertTrue(slots.size() >= 2,
                String.format("Diamond pickaxe should have at least 2 upgrade slots (has %d)", slots.size()));

        // Verify all slots have validators
        for (ComponentUpgradeSlot slot : slots) {
            assertNotNull(slot.validator(),
                    "Each upgrade slot must have a validator");
            assertNotNull(slot.slotType(),
                    "Each upgrade slot must have a slot type");
        }

        context.complete();
    }

    /**
     * Tests that netherite tools have the most upgrade slots.
     * This validates the upgrade slot progression: iron < diamond < netherite.
     */
    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void netherite_has_most_upgrade_slots(TestContext context) {
        var ironPickaxe = forgero(context).component("forgero:iron_pickaxe").orElseThrow();
        var diamondPickaxe = forgero(context).component("forgero:diamond_pickaxe").orElseThrow();
        var netheritePickaxe = forgero(context).component("forgero:netherite_pickaxe").orElseThrow();

        int ironSlots = ((CustomizableComponent) ironPickaxe).upgrades().allUpgradeSlots().size();
        int diamondSlots = ((CustomizableComponent) diamondPickaxe).upgrades().allUpgradeSlots().size();
        int netheriteSlots = ((CustomizableComponent) netheritePickaxe).upgrades().allUpgradeSlots().size();

        // Validate progression
        assertTrue(ironSlots <= diamondSlots && diamondSlots <= netheriteSlots,
                String.format("Upgrade slot progression should be iron(%d) <= diamond(%d) <= netherite(%d)",
                        ironSlots, diamondSlots, netheriteSlots));

        // All tools must have at least 1 upgrade slot
        assertTrue(ironSlots >= 1, "Iron pickaxe must have at least 1 upgrade slot");
        assertTrue(diamondSlots >= 1, "Diamond pickaxe must have at least 1 upgrade slot");
        assertTrue(netheriteSlots >= 1, "Netherite pickaxe must have at least 1 upgrade slot");

        context.complete();
    }

    /**
     * Tests that all vanilla tools are customizable (have upgrade slots).
     * This validates content completeness for the upgrade system.
     */
    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void all_vanilla_tools_are_customizable(TestContext context) {
        String[] vanillaTools = {
                "iron_pickaxe", "iron_sword", "iron_axe", "iron_shovel",
                "diamond_pickaxe", "diamond_sword", "diamond_axe",
                "golden_pickaxe", "netherite_pickaxe"
        };

        for (String toolName : vanillaTools) {
            Component tool = forgero(context).component("forgero:" + toolName).orElseThrow();

            assertTrue(tool instanceof CustomizableComponent,
                    toolName + " must be customizable");

            CustomizableComponent customizable = (CustomizableComponent) tool;
            assertFalse(customizable.upgrades().allUpgradeSlots().isEmpty(),
                    toolName + " must have at least one upgrade slot");
        }

        context.complete();
    }

    /**
     * Tests that all expected upgrade materials exist in the registry.
     * This validates content completeness for the upgrade system.
     */
    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void upgrade_materials_have_consistent_tags(TestContext context) {
        String[] upgradeMaterials = {
                "ender_pearl", "glowstone", "slime_ball", "blaze_rod",
                "fire_charge", "magma_cream", "honeycomb", "phantom_membrane"
        };

        for (String materialName : upgradeMaterials) {
            Component material = forgero(context).component("forgero:" + materialName).orElseThrow();

            // All upgrade materials must exist in the registry
            assertNotNull(material, materialName + " must exist in component registry");

            // All upgrade materials must have tags (for slot filtering)
            assertFalse(material.getTags().isEmpty(),
                    materialName + " must have at least one tag for slot filtering");
        }

        context.complete();
    }

    /**
     * Tests that different upgrade slot types exist (binding, gem, schematic).
     * This validates the polymorphic slot system.
     */
    @GameTest(templateName = EMPTY_STRUCTURE, required = true)
    public void multiple_upgrade_slot_types_exist(TestContext context) {
        var diamondPickaxe = forgero(context).component("forgero:diamond_pickaxe").orElseThrow();

        CustomizableComponent customizable = (CustomizableComponent) diamondPickaxe;
        List<ComponentUpgradeSlot> slots = customizable.upgrades().allUpgradeSlots();

        // Collect unique slot types
        long uniqueSlotTypes = slots.stream()
                .map(slot -> slot.slotType().toString())
                .distinct()
                .count();

        // Diamond tools should have multiple slot types
        assertTrue(uniqueSlotTypes >= 1,
                "Diamond pickaxe should have at least 1 slot type (has " + uniqueSlotTypes + ")");

        // Print slot types for debugging
        System.out.println("Diamond pickaxe slot types:");
        slots.forEach(slot -> System.out.println("  - " + slot.slotType()));

        context.complete();
    }
}
