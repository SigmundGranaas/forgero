package com.sigmundgranaas.repairkit.gametest;

import com.sigmundgranaas.repairkit.RepairKitItems;
import com.sigmundgranaas.repairkit.item.RepairKitItem;
import com.sigmundgranaas.repairkit.item.RepairKitTier;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;

/**
 * Gametests for tier multiplier functionality.
 */
public class TierMultiplierTest implements FabricGameTest {

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testScrappyTierMultiplier(TestContext context) {
        RepairKitTier tier = RepairKitTier.SCRAPPY;

        context.assertTrue(tier.getRepairMultiplier() == 0.25f, "Scrappy tier should have 25% multiplier");

        int repairAmount = tier.calculateRepairAmount(100);
        context.assertTrue(repairAmount == 25, "Scrappy should repair 25 durability for 100 max");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testStandardTierMultiplier(TestContext context) {
        RepairKitTier tier = RepairKitTier.STANDARD;

        context.assertTrue(tier.getRepairMultiplier() == 0.50f, "Standard tier should have 50% multiplier");

        int repairAmount = tier.calculateRepairAmount(100);
        context.assertTrue(repairAmount == 50, "Standard should repair 50 durability for 100 max");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testRefinedTierMultiplier(TestContext context) {
        RepairKitTier tier = RepairKitTier.REFINED;

        context.assertTrue(tier.getRepairMultiplier() == 0.75f, "Refined tier should have 75% multiplier");

        int repairAmount = tier.calculateRepairAmount(100);
        context.assertTrue(repairAmount == 75, "Refined should repair 75 durability for 100 max");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testMastercraftTierMultiplier(TestContext context) {
        RepairKitTier tier = RepairKitTier.MASTERCRAFT;

        context.assertTrue(tier.getRepairMultiplier() == 1.0f, "Mastercraft tier should have 100% multiplier");

        int repairAmount = tier.calculateRepairAmount(100);
        context.assertTrue(repairAmount == 100, "Mastercraft should repair 100 durability for 100 max");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testTierRepairAmountsWithRealTool(TestContext context) {
        ItemStack ironPickaxe = new ItemStack(Items.IRON_PICKAXE);
        int maxDamage = ironPickaxe.getMaxDamage(); // 250 for iron

        // Calculate expected repair amounts
        int scrappyRepair = RepairKitTier.SCRAPPY.calculateRepairAmount(maxDamage);
        int standardRepair = RepairKitTier.STANDARD.calculateRepairAmount(maxDamage);
        int refinedRepair = RepairKitTier.REFINED.calculateRepairAmount(maxDamage);
        int mastercraftRepair = RepairKitTier.MASTERCRAFT.calculateRepairAmount(maxDamage);

        // Iron pickaxe has 250 durability
        context.assertTrue(scrappyRepair == 62, "Scrappy should repair ~62 for iron pickaxe (250 * 0.25)");
        context.assertTrue(standardRepair == 125, "Standard should repair 125 for iron pickaxe (250 * 0.5)");
        context.assertTrue(refinedRepair == 187, "Refined should repair ~187 for iron pickaxe (250 * 0.75)");
        context.assertTrue(mastercraftRepair == 250, "Mastercraft should repair 250 for iron pickaxe (250 * 1.0)");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testAllTiersRegistered(TestContext context) {
        context.assertTrue(RepairKitItems.SCRAPPY_REPAIR_KIT != null, "Scrappy kit should be registered");
        context.assertTrue(RepairKitItems.REPAIR_KIT != null, "Standard kit should be registered");
        context.assertTrue(RepairKitItems.REFINED_REPAIR_KIT != null, "Refined kit should be registered");
        context.assertTrue(RepairKitItems.MASTERCRAFT_REPAIR_KIT != null, "Mastercraft kit should be registered");

        context.assertTrue(RepairKitItems.SCRAPPY_REPAIR_KIT.getTier() == RepairKitTier.SCRAPPY,
                "Scrappy kit should have SCRAPPY tier");
        context.assertTrue(RepairKitItems.REPAIR_KIT.getTier() == RepairKitTier.STANDARD,
                "Standard kit should have STANDARD tier");
        context.assertTrue(RepairKitItems.REFINED_REPAIR_KIT.getTier() == RepairKitTier.REFINED,
                "Refined kit should have REFINED tier");
        context.assertTrue(RepairKitItems.MASTERCRAFT_REPAIR_KIT.getTier() == RepairKitTier.MASTERCRAFT,
                "Mastercraft kit should have MASTERCRAFT tier");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testMinimumRepairAmount(TestContext context) {
        // Even with very low durability, should repair at least 1
        int repairAmount = RepairKitTier.SCRAPPY.calculateRepairAmount(1);
        context.assertTrue(repairAmount >= 1, "Should repair at least 1 durability");

        repairAmount = RepairKitTier.SCRAPPY.calculateRepairAmount(2);
        context.assertTrue(repairAmount >= 1, "Should repair at least 1 durability");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testTierProgression(TestContext context) {
        int maxDurability = 1000;

        int scrappy = RepairKitTier.SCRAPPY.calculateRepairAmount(maxDurability);
        int standard = RepairKitTier.STANDARD.calculateRepairAmount(maxDurability);
        int refined = RepairKitTier.REFINED.calculateRepairAmount(maxDurability);
        int mastercraft = RepairKitTier.MASTERCRAFT.calculateRepairAmount(maxDurability);

        context.assertTrue(scrappy < standard, "Scrappy should repair less than Standard");
        context.assertTrue(standard < refined, "Standard should repair less than Refined");
        context.assertTrue(refined < mastercraft, "Refined should repair less than Mastercraft");

        context.complete();
    }
}
