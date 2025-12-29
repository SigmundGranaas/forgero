package com.sigmundgranaas.repairkit.gametest;

import com.sigmundgranaas.repairkit.RepairKitItems;
import com.sigmundgranaas.repairkit.item.RepairKitItem;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;

/**
 * Gametests for filling repair kits with materials.
 */
public class RepairKitFillTest implements FabricGameTest {

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testEmptyKitHasNoMaterials(TestContext context) {
        ItemStack kit = new ItemStack(RepairKitItems.REPAIR_KIT);

        context.assertTrue(RepairKitItem.isEmpty(kit), "New kit should be empty");
        context.assertTrue(RepairKitItem.getMaterialCount(kit) == 0, "Material count should be 0");
        context.assertTrue(RepairKitItem.getStoredMaterial(kit).isEmpty(), "Should have no stored material");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testFillKitWithMaterials(TestContext context) {
        ItemStack kit = new ItemStack(RepairKitItems.REPAIR_KIT);
        Identifier ironIngot = new Identifier("minecraft", "iron_ingot");

        int added = RepairKitItem.addMaterials(kit, ironIngot, 32);

        context.assertTrue(added == 32, "Should add 32 materials");
        context.assertTrue(RepairKitItem.getMaterialCount(kit) == 32, "Count should be 32");
        context.assertTrue(RepairKitItem.getStoredMaterial(kit).isPresent(), "Should have stored material");
        context.assertTrue(RepairKitItem.getStoredMaterial(kit).get().equals(ironIngot), "Material should be iron_ingot");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testFillKitToCapacity(TestContext context) {
        ItemStack kit = new ItemStack(RepairKitItems.REPAIR_KIT);
        Identifier ironIngot = new Identifier("minecraft", "iron_ingot");

        int added = RepairKitItem.addMaterials(kit, ironIngot, 100);

        context.assertTrue(added == 64, "Should only add up to 64 materials");
        context.assertTrue(RepairKitItem.getMaterialCount(kit) == 64, "Count should be capped at 64");
        context.assertTrue(RepairKitItem.isFull(kit), "Kit should be full");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testCannotMixMaterials(TestContext context) {
        ItemStack kit = new ItemStack(RepairKitItems.REPAIR_KIT);
        Identifier ironIngot = new Identifier("minecraft", "iron_ingot");
        Identifier diamond = new Identifier("minecraft", "diamond");

        RepairKitItem.addMaterials(kit, ironIngot, 32);
        int added = RepairKitItem.addMaterials(kit, diamond, 10);

        context.assertTrue(added == 0, "Should not add different material");
        context.assertTrue(RepairKitItem.getMaterialCount(kit) == 32, "Count should remain 32");
        context.assertTrue(RepairKitItem.getStoredMaterial(kit).get().equals(ironIngot),
                "Material should still be iron_ingot");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testPartialFill(TestContext context) {
        ItemStack kit = new ItemStack(RepairKitItems.REPAIR_KIT);
        Identifier ironIngot = new Identifier("minecraft", "iron_ingot");

        RepairKitItem.addMaterials(kit, ironIngot, 20);
        int added = RepairKitItem.addMaterials(kit, ironIngot, 30);

        context.assertTrue(added == 30, "Should add 30 more materials");
        context.assertTrue(RepairKitItem.getMaterialCount(kit) == 50, "Count should be 50");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testUseMaterial(TestContext context) {
        ItemStack kit = new ItemStack(RepairKitItems.REPAIR_KIT);
        Identifier ironIngot = new Identifier("minecraft", "iron_ingot");

        RepairKitItem.addMaterials(kit, ironIngot, 5);
        boolean used = RepairKitItem.useMaterial(kit);

        context.assertTrue(used, "Should use material successfully");
        context.assertTrue(RepairKitItem.getMaterialCount(kit) == 4, "Count should be 4");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testUseLastMaterial(TestContext context) {
        ItemStack kit = new ItemStack(RepairKitItems.REPAIR_KIT);
        Identifier ironIngot = new Identifier("minecraft", "iron_ingot");

        RepairKitItem.addMaterials(kit, ironIngot, 1);
        RepairKitItem.useMaterial(kit);

        context.assertTrue(RepairKitItem.isEmpty(kit), "Kit should be empty after using last material");
        context.assertTrue(RepairKitItem.getMaterialCount(kit) == 0, "Count should be 0");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testCannotUseFromEmptyKit(TestContext context) {
        ItemStack kit = new ItemStack(RepairKitItems.REPAIR_KIT);

        boolean used = RepairKitItem.useMaterial(kit);

        context.assertTrue(!used, "Should not be able to use material from empty kit");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testItemBarVisibility(TestContext context) {
        ItemStack emptyKit = new ItemStack(RepairKitItems.REPAIR_KIT);
        ItemStack filledKit = new ItemStack(RepairKitItems.REPAIR_KIT);

        RepairKitItem.addMaterials(filledKit, new Identifier("minecraft", "iron_ingot"), 32);

        context.assertTrue(!RepairKitItems.REPAIR_KIT.isItemBarVisible(emptyKit),
                "Empty kit should not show item bar");
        context.assertTrue(RepairKitItems.REPAIR_KIT.isItemBarVisible(filledKit),
                "Filled kit should show item bar");

        context.complete();
    }
}
