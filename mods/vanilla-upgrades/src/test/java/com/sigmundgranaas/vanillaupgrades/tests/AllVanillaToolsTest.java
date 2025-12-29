package com.sigmundgranaas.vanillaupgrades.tests;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
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
 * Comprehensive tests for ALL vanilla tools in vanilla-upgrades mod.
 * Validates that all 30 vanilla tools convert to components correctly.
 */
public class AllVanillaToolsTest {

    private static final String BATCH = "vanilla_all_tools";

    private static ForgeroServices getServices() {
        return ForgeroInitializedCallback.getServices()
                .orElseThrow(() -> new AssertionError("ForgeroServices not available"));
    }

    // ==================== Pickaxe Tests ====================

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void wooden_pickaxe_converts(TestContext context) {
        assertToolConverts(Items.WOODEN_PICKAXE, "pickaxe");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void stone_pickaxe_converts(TestContext context) {
        assertToolConverts(Items.STONE_PICKAXE, "pickaxe");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void iron_pickaxe_converts(TestContext context) {
        assertToolConverts(Items.IRON_PICKAXE, "pickaxe");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void golden_pickaxe_converts(TestContext context) {
        assertToolConverts(Items.GOLDEN_PICKAXE, "pickaxe");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void diamond_pickaxe_converts(TestContext context) {
        assertToolConverts(Items.DIAMOND_PICKAXE, "pickaxe");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void netherite_pickaxe_converts(TestContext context) {
        assertToolConverts(Items.NETHERITE_PICKAXE, "pickaxe");
        context.complete();
    }

    // ==================== Sword Tests ====================

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void wooden_sword_converts(TestContext context) {
        assertToolConverts(Items.WOODEN_SWORD, "sword");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void stone_sword_converts(TestContext context) {
        assertToolConverts(Items.STONE_SWORD, "sword");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void iron_sword_converts(TestContext context) {
        assertToolConverts(Items.IRON_SWORD, "sword");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void golden_sword_converts(TestContext context) {
        assertToolConverts(Items.GOLDEN_SWORD, "sword");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void diamond_sword_converts(TestContext context) {
        assertToolConverts(Items.DIAMOND_SWORD, "sword");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void netherite_sword_converts(TestContext context) {
        assertToolConverts(Items.NETHERITE_SWORD, "sword");
        context.complete();
    }

    // ==================== Axe Tests ====================

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void wooden_axe_converts(TestContext context) {
        assertToolConverts(Items.WOODEN_AXE, "axe");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void stone_axe_converts(TestContext context) {
        assertToolConverts(Items.STONE_AXE, "axe");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void iron_axe_converts(TestContext context) {
        assertToolConverts(Items.IRON_AXE, "axe");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void golden_axe_converts(TestContext context) {
        assertToolConverts(Items.GOLDEN_AXE, "axe");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void diamond_axe_converts(TestContext context) {
        assertToolConverts(Items.DIAMOND_AXE, "axe");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void netherite_axe_converts(TestContext context) {
        assertToolConverts(Items.NETHERITE_AXE, "axe");
        context.complete();
    }

    // ==================== Shovel Tests ====================

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void wooden_shovel_converts(TestContext context) {
        assertToolConverts(Items.WOODEN_SHOVEL, "shovel");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void stone_shovel_converts(TestContext context) {
        assertToolConverts(Items.STONE_SHOVEL, "shovel");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void iron_shovel_converts(TestContext context) {
        assertToolConverts(Items.IRON_SHOVEL, "shovel");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void golden_shovel_converts(TestContext context) {
        assertToolConverts(Items.GOLDEN_SHOVEL, "shovel");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void diamond_shovel_converts(TestContext context) {
        assertToolConverts(Items.DIAMOND_SHOVEL, "shovel");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void netherite_shovel_converts(TestContext context) {
        assertToolConverts(Items.NETHERITE_SHOVEL, "shovel");
        context.complete();
    }

    // ==================== Hoe Tests ====================

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void wooden_hoe_converts(TestContext context) {
        assertToolConverts(Items.WOODEN_HOE, "hoe");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void stone_hoe_converts(TestContext context) {
        assertToolConverts(Items.STONE_HOE, "hoe");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void iron_hoe_converts(TestContext context) {
        assertToolConverts(Items.IRON_HOE, "hoe");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void golden_hoe_converts(TestContext context) {
        assertToolConverts(Items.GOLDEN_HOE, "hoe");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void diamond_hoe_converts(TestContext context) {
        assertToolConverts(Items.DIAMOND_HOE, "hoe");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void netherite_hoe_converts(TestContext context) {
        assertToolConverts(Items.NETHERITE_HOE, "hoe");
        context.complete();
    }

    // ==================== Helper Methods ====================

    private void assertToolConverts(Item item, String toolType) {
        ItemStack stack = new ItemStack(item);
        Optional<Component> component = getServices().converter().toComponent(stack);

        assertTrue(component.isPresent(),
                item.toString() + " must convert to component");

        Component comp = component.get();
        assertNotNull(comp.id(), "Component must have ID");

        // Verify tool has correct tag
        boolean hasToolTag = comp.getTags().stream()
                .anyMatch(tag -> tag.name().contains(toolType));
        assertTrue(hasToolTag,
                item.toString() + " should have " + toolType + " tag");
    }

    // ==================== Aggregate Tests ====================

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void all_30_vanilla_tools_convert(TestContext context) {
        List<Item> allVanillaTools = List.of(
                // Pickaxes
                Items.WOODEN_PICKAXE, Items.STONE_PICKAXE, Items.IRON_PICKAXE,
                Items.GOLDEN_PICKAXE, Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE,
                // Swords
                Items.WOODEN_SWORD, Items.STONE_SWORD, Items.IRON_SWORD,
                Items.GOLDEN_SWORD, Items.DIAMOND_SWORD, Items.NETHERITE_SWORD,
                // Axes
                Items.WOODEN_AXE, Items.STONE_AXE, Items.IRON_AXE,
                Items.GOLDEN_AXE, Items.DIAMOND_AXE, Items.NETHERITE_AXE,
                // Shovels
                Items.WOODEN_SHOVEL, Items.STONE_SHOVEL, Items.IRON_SHOVEL,
                Items.GOLDEN_SHOVEL, Items.DIAMOND_SHOVEL, Items.NETHERITE_SHOVEL,
                // Hoes
                Items.WOODEN_HOE, Items.STONE_HOE, Items.IRON_HOE,
                Items.GOLDEN_HOE, Items.DIAMOND_HOE, Items.NETHERITE_HOE
        );

        int convertedCount = 0;
        for (Item item : allVanillaTools) {
            ItemStack stack = new ItemStack(item);
            Optional<Component> component = getServices().converter().toComponent(stack);
            if (component.isPresent()) {
                convertedCount++;
            }
        }

        assertEquals(30, convertedCount,
                "All 30 vanilla tools must convert. Converted: " + convertedCount);

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void all_vanilla_tools_have_upgrade_slots(TestContext context) {
        List<Item> allVanillaTools = List.of(
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

        int toolsWithUpgrades = 0;
        for (Item item : allVanillaTools) {
            ItemStack stack = new ItemStack(item);
            Optional<Component> component = getServices().converter().toComponent(stack);

            if (component.isPresent() && component.get() instanceof CustomizableComponent custom) {
                if (!custom.upgrades().isEmpty()) {
                    toolsWithUpgrades++;
                }
            }
        }

        assertEquals(30, toolsWithUpgrades,
                "All 30 vanilla tools must have upgrade slots. Tools with upgrades: " + toolsWithUpgrades);

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void all_vanilla_tools_have_binding_slot(TestContext context) {
        List<Item> allVanillaTools = List.of(
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

        int toolsWithBinding = 0;
        for (Item item : allVanillaTools) {
            ItemStack stack = new ItemStack(item);
            Optional<Component> component = getServices().converter().toComponent(stack);

            if (component.isPresent() && component.get() instanceof CustomizableComponent custom) {
                boolean hasBinding = custom.upgrades().allUpgradeSlots().stream()
                        .anyMatch(slot -> slot.description().contains("binding") ||
                                         slot.id().name().contains("binding"));
                if (hasBinding) {
                    toolsWithBinding++;
                }
            }
        }

        assertEquals(30, toolsWithBinding,
                "All 30 vanilla tools must have binding slot. Tools with binding: " + toolsWithBinding);

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, required = true)
    public void all_vanilla_tools_roundtrip_correctly(TestContext context) {
        List<Item> allVanillaTools = List.of(
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

        int successfulRoundtrips = 0;
        for (Item item : allVanillaTools) {
            ItemStack original = new ItemStack(item);
            Optional<Component> component = getServices().converter().toComponent(original);

            if (component.isPresent()) {
                Optional<ItemStack> converted = getServices().converter().toStack(component.get());
                if (converted.isPresent() && converted.get().getItem() == item) {
                    successfulRoundtrips++;
                }
            }
        }

        assertEquals(30, successfulRoundtrips,
                "All 30 vanilla tools must roundtrip correctly. Successful: " + successfulRoundtrips);

        context.complete();
    }
}
