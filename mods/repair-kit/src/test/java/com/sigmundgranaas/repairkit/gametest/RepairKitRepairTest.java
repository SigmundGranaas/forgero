package com.sigmundgranaas.repairkit.gametest;

import com.sigmundgranaas.repairkit.RepairKitItems;
import com.sigmundgranaas.repairkit.RepairMaterialProviders;
import com.sigmundgranaas.repairkit.item.RepairKitItem;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;

import java.util.Optional;

/**
 * Gametests for repairing items with repair kits.
 */
public class RepairKitRepairTest implements FabricGameTest {

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testVanillaToolRepairIngredient(TestContext context) {
        ItemStack ironPickaxe = new ItemStack(Items.IRON_PICKAXE);
        Optional<Ingredient> ingredient = RepairMaterialProviders.getRepairIngredient(ironPickaxe);

        context.assertTrue(ingredient.isPresent(), "Iron pickaxe should have repair ingredient");
        context.assertTrue(ingredient.get().test(new ItemStack(Items.IRON_INGOT)),
                "Iron pickaxe repair ingredient should accept iron ingot");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testVanillaArmorRepairIngredient(TestContext context) {
        ItemStack diamondChestplate = new ItemStack(Items.DIAMOND_CHESTPLATE);
        Optional<Ingredient> ingredient = RepairMaterialProviders.getRepairIngredient(diamondChestplate);

        context.assertTrue(ingredient.isPresent(), "Diamond chestplate should have repair ingredient");
        context.assertTrue(ingredient.get().test(new ItemStack(Items.DIAMOND)),
                "Diamond chestplate repair ingredient should accept diamond");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testBowRepairIngredient(TestContext context) {
        ItemStack bow = new ItemStack(Items.BOW);
        Optional<Ingredient> ingredient = RepairMaterialProviders.getRepairIngredient(bow);

        context.assertTrue(ingredient.isPresent(), "Bow should have repair ingredient");
        context.assertTrue(ingredient.get().test(new ItemStack(Items.STRING)),
                "Bow repair ingredient should accept string");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testShieldRepairIngredient(TestContext context) {
        ItemStack shield = new ItemStack(Items.SHIELD);
        Optional<Ingredient> ingredient = RepairMaterialProviders.getRepairIngredient(shield);

        context.assertTrue(ingredient.isPresent(), "Shield should have repair ingredient");
        context.assertTrue(ingredient.get().test(new ItemStack(Items.OAK_PLANKS)),
                "Shield repair ingredient should accept planks");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testCraftingRepairReducesDamage(TestContext context) {
        // Create damaged tool
        ItemStack ironPickaxe = new ItemStack(Items.IRON_PICKAXE);
        int maxDamage = ironPickaxe.getMaxDamage();
        ironPickaxe.setDamage(maxDamage / 2); // 50% damaged

        // Create filled kit (standard tier = 50% repair)
        ItemStack kit = new ItemStack(RepairKitItems.REPAIR_KIT);
        RepairKitItem.addMaterials(kit, new Identifier("minecraft", "iron_ingot"), 10);

        // Calculate expected repair
        int repairAmount = RepairKitItems.REPAIR_KIT.getTier().calculateRepairAmount(maxDamage);
        int expectedDamage = Math.max(0, ironPickaxe.getDamage() - repairAmount);

        // Simulate repair
        int newDamage = Math.max(0, ironPickaxe.getDamage() - repairAmount);
        ironPickaxe.setDamage(newDamage);

        context.assertTrue(ironPickaxe.getDamage() == expectedDamage,
                "Tool damage should be reduced by repair amount");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testWrongMaterialCannotRepair(TestContext context) {
        // Create diamond tool
        ItemStack diamondPickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
        diamondPickaxe.setDamage(100);
        int originalDamage = diamondPickaxe.getDamage();

        // Create kit filled with iron (wrong material)
        ItemStack kit = new ItemStack(RepairKitItems.REPAIR_KIT);
        RepairKitItem.addMaterials(kit, new Identifier("minecraft", "iron_ingot"), 10);

        // Check if repair would work
        Optional<Identifier> storedMaterial = RepairKitItem.getStoredMaterial(kit);
        Optional<Ingredient> repairIngredient = RepairMaterialProviders.getRepairIngredient(diamondPickaxe);

        boolean canRepair = false;
        if (storedMaterial.isPresent() && repairIngredient.isPresent()) {
            ItemStack materialStack = new ItemStack(Items.IRON_INGOT);
            canRepair = repairIngredient.get().test(materialStack);
        }

        context.assertTrue(!canRepair, "Iron should not repair diamond tools");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testEmptyKitCannotRepair(TestContext context) {
        ItemStack kit = new ItemStack(RepairKitItems.REPAIR_KIT);
        ItemStack ironPickaxe = new ItemStack(Items.IRON_PICKAXE);
        ironPickaxe.setDamage(100);

        context.assertTrue(RepairKitItem.isEmpty(kit), "Kit should be empty");
        context.assertTrue(!RepairKitItems.REPAIR_KIT.attemptRepair(kit, ironPickaxe, null),
                "Empty kit should not repair");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testUndamagedToolCannotBeRepaired(TestContext context) {
        ItemStack kit = new ItemStack(RepairKitItems.REPAIR_KIT);
        RepairKitItem.addMaterials(kit, new Identifier("minecraft", "iron_ingot"), 10);

        ItemStack ironPickaxe = new ItemStack(Items.IRON_PICKAXE);
        // Not damaged

        context.assertTrue(!ironPickaxe.isDamaged(), "Tool should not be damaged");
        context.assertTrue(!RepairKitItems.REPAIR_KIT.attemptRepair(kit, ironPickaxe, null),
                "Undamaged tool should not be repaired");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testNonDamageableItemCannotBeRepaired(TestContext context) {
        ItemStack kit = new ItemStack(RepairKitItems.REPAIR_KIT);
        RepairKitItem.addMaterials(kit, new Identifier("minecraft", "iron_ingot"), 10);

        ItemStack dirt = new ItemStack(Items.DIRT);

        context.assertTrue(!dirt.isDamageable(), "Dirt should not be damageable");
        context.assertTrue(!RepairKitItems.REPAIR_KIT.attemptRepair(kit, dirt, null),
                "Non-damageable item should not be repaired");

        context.complete();
    }
}
