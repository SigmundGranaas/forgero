package com.sigmundgranaas.vanillaupgrades.tests;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.loader.api.ForgeroInitializedCallback;
import com.sigmundgranaas.forgero.loader.api.ForgeroServices;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for vanilla tool attribute values.
 * Validates that vanilla tools have correct Minecraft-accurate attributes.
 */
public class VanillaToolAttributesTest {

    private static final String BATCH = "vanilla_tool_attributes";

    private static ForgeroServices getServices() {
        return ForgeroInitializedCallback.getServices()
                .orElseThrow(() -> new AssertionError("ForgeroServices not available"));
    }

    // ==================== Durability Tests ====================

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void iron_pickaxe_has_correct_durability(TestContext context) {
        assertDurabilityValue(Items.IRON_PICKAXE, 250);
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void diamond_pickaxe_has_correct_durability(TestContext context) {
        assertDurabilityValue(Items.DIAMOND_PICKAXE, 1561);
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void netherite_pickaxe_has_correct_durability(TestContext context) {
        assertDurabilityValue(Items.NETHERITE_PICKAXE, 2031);
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void wooden_sword_has_correct_durability(TestContext context) {
        assertDurabilityValue(Items.WOODEN_SWORD, 59);
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void stone_axe_has_correct_durability(TestContext context) {
        assertDurabilityValue(Items.STONE_AXE, 131);
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void golden_hoe_has_correct_durability(TestContext context) {
        assertDurabilityValue(Items.GOLDEN_HOE, 32);
        context.complete();
    }

    // ==================== Attack Damage Tests ====================

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void iron_sword_has_correct_attack_damage(TestContext context) {
        assertAttackDamageValue(Items.IRON_SWORD, 6);
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void diamond_sword_has_correct_attack_damage(TestContext context) {
        assertAttackDamageValue(Items.DIAMOND_SWORD, 7);
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void netherite_sword_has_correct_attack_damage(TestContext context) {
        assertAttackDamageValue(Items.NETHERITE_SWORD, 8);
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void iron_axe_has_correct_attack_damage(TestContext context) {
        assertAttackDamageValue(Items.IRON_AXE, 9);
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void diamond_axe_has_correct_attack_damage(TestContext context) {
        assertAttackDamageValue(Items.DIAMOND_AXE, 9);
        context.complete();
    }

    // ==================== Mining Speed Tests ====================

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void iron_pickaxe_has_correct_mining_speed(TestContext context) {
        assertMiningSpeedValue(Items.IRON_PICKAXE, 6);
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void diamond_pickaxe_has_correct_mining_speed(TestContext context) {
        assertMiningSpeedValue(Items.DIAMOND_PICKAXE, 8);
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void netherite_pickaxe_has_correct_mining_speed(TestContext context) {
        assertMiningSpeedValue(Items.NETHERITE_PICKAXE, 9);
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void golden_pickaxe_has_correct_mining_speed(TestContext context) {
        assertMiningSpeedValue(Items.GOLDEN_PICKAXE, 12);
        context.complete();
    }

    // ==================== Mining Level Tests ====================

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void iron_pickaxe_has_correct_mining_level(TestContext context) {
        assertMiningLevelValue(Items.IRON_PICKAXE, 2);
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void diamond_pickaxe_has_correct_mining_level(TestContext context) {
        assertMiningLevelValue(Items.DIAMOND_PICKAXE, 3);
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void netherite_pickaxe_has_correct_mining_level(TestContext context) {
        assertMiningLevelValue(Items.NETHERITE_PICKAXE, 4);
        context.complete();
    }

    // ==================== Tier Comparison Tests ====================

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void higher_tier_has_more_durability(TestContext context) {
        double woodenDurability = getDurabilityValue(Items.WOODEN_PICKAXE);
        double stoneDurability = getDurabilityValue(Items.STONE_PICKAXE);
        double ironDurability = getDurabilityValue(Items.IRON_PICKAXE);
        double diamondDurability = getDurabilityValue(Items.DIAMOND_PICKAXE);
        double netheriteDurability = getDurabilityValue(Items.NETHERITE_PICKAXE);

        assertTrue(woodenDurability < stoneDurability,
                "Stone should have more durability than wood");
        assertTrue(stoneDurability < ironDurability,
                "Iron should have more durability than stone");
        assertTrue(ironDurability < diamondDurability,
                "Diamond should have more durability than iron");
        assertTrue(diamondDurability < netheriteDurability,
                "Netherite should have more durability than diamond");

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void higher_tier_has_more_mining_speed(TestContext context) {
        double woodenSpeed = getMiningSpeedValue(Items.WOODEN_PICKAXE);
        double stoneSpeed = getMiningSpeedValue(Items.STONE_PICKAXE);
        double ironSpeed = getMiningSpeedValue(Items.IRON_PICKAXE);
        double diamondSpeed = getMiningSpeedValue(Items.DIAMOND_PICKAXE);
        double netheriteSpeed = getMiningSpeedValue(Items.NETHERITE_PICKAXE);

        // Golden pickaxe is an exception - fastest mining but low durability
        double goldenSpeed = getMiningSpeedValue(Items.GOLDEN_PICKAXE);

        assertTrue(woodenSpeed < stoneSpeed,
                "Stone should mine faster than wood");
        assertTrue(stoneSpeed < ironSpeed,
                "Iron should mine faster than stone");
        assertTrue(ironSpeed < diamondSpeed,
                "Diamond should mine faster than iron");
        assertTrue(diamondSpeed < netheriteSpeed,
                "Netherite should mine faster than diamond");
        assertTrue(goldenSpeed > diamondSpeed,
                "Golden should mine faster than diamond (special case)");

        context.complete();
    }

    // ==================== Aggregate Attribute Tests ====================

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void all_pickaxes_have_durability(TestContext context) {
        List<Item> pickaxes = List.of(
                Items.WOODEN_PICKAXE, Items.STONE_PICKAXE, Items.IRON_PICKAXE,
                Items.GOLDEN_PICKAXE, Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE
        );

        for (Item pickaxe : pickaxes) {
            double durability = getDurabilityValue(pickaxe);
            assertTrue(durability > 0,
                    pickaxe.toString() + " should have positive durability");
        }

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void all_swords_have_attack_damage(TestContext context) {
        List<Item> swords = List.of(
                Items.WOODEN_SWORD, Items.STONE_SWORD, Items.IRON_SWORD,
                Items.GOLDEN_SWORD, Items.DIAMOND_SWORD, Items.NETHERITE_SWORD
        );

        for (Item sword : swords) {
            double attackDamage = getAttackDamageValue(sword);
            assertTrue(attackDamage > 0,
                    sword.toString() + " should have positive attack damage");
        }

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void all_tools_have_positive_attributes(TestContext context) {
        List<Item> allTools = List.of(
                Items.WOODEN_PICKAXE, Items.STONE_PICKAXE, Items.IRON_PICKAXE,
                Items.GOLDEN_PICKAXE, Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE,
                Items.WOODEN_SWORD, Items.STONE_SWORD, Items.IRON_SWORD,
                Items.GOLDEN_SWORD, Items.DIAMOND_SWORD, Items.NETHERITE_SWORD,
                Items.WOODEN_AXE, Items.STONE_AXE, Items.IRON_AXE,
                Items.GOLDEN_AXE, Items.DIAMOND_AXE, Items.NETHERITE_AXE,
                Items.WOODEN_SHOVEL, Items.STONE_SHOVEL, Items.IRON_SHOVEL,
                Items.GOLDEN_SHOVEL, Items.DIAMOND_SHOVEL, Items.NETHERITE_SHOVEL,
                Items.WOODEN_HOE, Items.STONE_HOE, Items.IRON_HOE,
                Items.GOLDEN_HOE, Items.DIAMOND_HOE, Items.NETHERITE_HOE
        );

        for (Item tool : allTools) {
            double durability = getDurabilityValue(tool);
            assertTrue(durability > 0,
                    tool.toString() + " should have positive durability");
        }

        context.complete();
    }

    // ==================== Helper Methods ====================

    private void assertDurabilityValue(Item item, double expectedValue) {
        double actualValue = getDurabilityValue(item);
        assertEquals(expectedValue, actualValue, 0.1,
                item.toString() + " should have durability " + expectedValue);
    }

    private void assertAttackDamageValue(Item item, double expectedValue) {
        double actualValue = getAttackDamageValue(item);
        assertEquals(expectedValue, actualValue, 0.1,
                item.toString() + " should have attack damage " + expectedValue);
    }

    private void assertMiningSpeedValue(Item item, double expectedValue) {
        double actualValue = getMiningSpeedValue(item);
        assertEquals(expectedValue, actualValue, 0.1,
                item.toString() + " should have mining speed " + expectedValue);
    }

    private void assertMiningLevelValue(Item item, double expectedValue) {
        double actualValue = getMiningLevelValue(item);
        assertEquals(expectedValue, actualValue, 0.1,
                item.toString() + " should have mining level " + expectedValue);
    }

    private double getDurabilityValue(Item item) {
        return getAttributeValue(item, "durability");
    }

    private double getAttackDamageValue(Item item) {
        return getAttributeValue(item, "attack_damage");
    }

    private double getMiningSpeedValue(Item item) {
        return getAttributeValue(item, "mining_speed");
    }

    private double getMiningLevelValue(Item item) {
        return getAttributeValue(item, "mining_level");
    }

    private double getAttributeValue(Item item, String attributeName) {
        ItemStack stack = new ItemStack(item);
        Optional<Component> componentOpt = getServices().converter().toComponent(stack);

        if (componentOpt.isEmpty()) {
            fail(item.toString() + " must convert to component");
            return 0;
        }

        Component component = componentOpt.get();
        return component.properties(Attribute.KEY).stream()
                .filter(attr -> attr.type().name().contains(attributeName))
                .findFirst()
                .map(attr -> (double) attr.value())
                .orElse(0.0);
    }
}
