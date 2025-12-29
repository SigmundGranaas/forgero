package com.sigmundgranaas.repairkit.gametest;

import com.mojang.authlib.GameProfile;
import com.sigmundgranaas.repairkit.RepairKitItems;
import com.sigmundgranaas.repairkit.item.RepairKitItem;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

/**
 * Gametests for player interactions with repair kits.
 * Tests actual player mechanics like filling kits and repairing tools.
 */
public class PlayerInteractionTest implements FabricGameTest {

    /**
     * Creates a mock player at the given position.
     */
    private static ServerPlayerEntity createMockPlayer(TestContext context, BlockPos pos) {
        context.getWorld().getServer().setDemo(false);
        ServerPlayerEntity player = new ServerPlayerEntity(
                context.getWorld().getServer(),
                context.getWorld(),
                new GameProfile(UUID.randomUUID(), "test-player")
        );
        player.networkHandler = new ServerPlayNetworkHandler(
                context.getWorld().getServer(),
                new ClientConnection(NetworkSide.CLIENTBOUND),
                player
        );
        BlockPos absPos = context.getAbsolutePos(pos);
        player.setPos(absPos.getX(), absPos.getY(), absPos.getZ());
        return player;
    }

    // ===== Fill Kit Tests =====

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testPlayerFillsKitWithMaterials(TestContext context) {
        ServerPlayerEntity player = createMockPlayer(context, new BlockPos(0, 1, 0));

        // Setup: kit in main hand, iron ingots in offhand
        ItemStack kit = new ItemStack(RepairKitItems.REPAIR_KIT);
        ItemStack materials = new ItemStack(Items.IRON_INGOT, 32);

        player.setStackInHand(Hand.MAIN_HAND, kit);
        player.setStackInHand(Hand.OFF_HAND, materials);

        // Verify initial state
        context.assertTrue(RepairKitItem.isEmpty(kit), "Kit should start empty");
        context.assertTrue(materials.getCount() == 32, "Should have 32 iron ingots");

        // Trigger the use action on the kit
        RepairKitItems.REPAIR_KIT.use(context.getWorld(), player, Hand.MAIN_HAND);

        // Simulate holding use for full duration
        ItemStack mainHandStack = player.getStackInHand(Hand.MAIN_HAND);
        ItemStack resultStack = RepairKitItems.REPAIR_KIT.finishUsing(mainHandStack, context.getWorld(), player);

        // Verify materials were transferred
        int storedCount = RepairKitItem.getMaterialCount(resultStack);
        int remainingMaterials = player.getStackInHand(Hand.OFF_HAND).getCount();

        context.assertTrue(storedCount == 32, "Kit should have 32 materials stored, got: " + storedCount);
        context.assertTrue(remainingMaterials == 0, "Offhand should be empty, got: " + remainingMaterials);
        context.assertTrue(
                RepairKitItem.getStoredMaterial(resultStack).isPresent() &&
                        RepairKitItem.getStoredMaterial(resultStack).get().equals(new Identifier("minecraft", "iron_ingot")),
                "Stored material should be iron_ingot"
        );

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testPlayerFillsKitPartially(TestContext context) {
        ServerPlayerEntity player = createMockPlayer(context, new BlockPos(0, 1, 0));

        // Setup: kit with some materials, more in offhand
        ItemStack kit = new ItemStack(RepairKitItems.REPAIR_KIT);
        RepairKitItem.addMaterials(kit, new Identifier("minecraft", "iron_ingot"), 50);
        ItemStack materials = new ItemStack(Items.IRON_INGOT, 32);

        player.setStackInHand(Hand.MAIN_HAND, kit);
        player.setStackInHand(Hand.OFF_HAND, materials);

        // Trigger fill action
        RepairKitItems.REPAIR_KIT.use(context.getWorld(), player, Hand.MAIN_HAND);
        ItemStack resultStack = RepairKitItems.REPAIR_KIT.finishUsing(kit, context.getWorld(), player);

        // Should only add 14 (to reach max of 64)
        int storedCount = RepairKitItem.getMaterialCount(resultStack);
        int remainingMaterials = player.getStackInHand(Hand.OFF_HAND).getCount();

        context.assertTrue(storedCount == 64, "Kit should be full at 64, got: " + storedCount);
        context.assertTrue(remainingMaterials == 18, "Should have 18 materials left (32 - 14), got: " + remainingMaterials);

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testPlayerCannotMixMaterials(TestContext context) {
        ServerPlayerEntity player = createMockPlayer(context, new BlockPos(0, 1, 0));

        // Setup: kit with iron, diamonds in offhand
        ItemStack kit = new ItemStack(RepairKitItems.REPAIR_KIT);
        RepairKitItem.addMaterials(kit, new Identifier("minecraft", "iron_ingot"), 10);
        ItemStack materials = new ItemStack(Items.DIAMOND, 10);

        player.setStackInHand(Hand.MAIN_HAND, kit);
        player.setStackInHand(Hand.OFF_HAND, materials);

        // Try to fill with wrong material
        RepairKitItems.REPAIR_KIT.use(context.getWorld(), player, Hand.MAIN_HAND);
        ItemStack resultStack = RepairKitItems.REPAIR_KIT.finishUsing(kit, context.getWorld(), player);

        // Should still have 10 iron
        int storedCount = RepairKitItem.getMaterialCount(resultStack);
        context.assertTrue(storedCount == 10, "Kit should still have only 10 iron, got: " + storedCount);
        context.assertTrue(
                RepairKitItem.getStoredMaterial(resultStack).get().equals(new Identifier("minecraft", "iron_ingot")),
                "Material should still be iron"
        );

        context.complete();
    }

    // ===== Offhand Repair Tests =====

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testPlayerRepairsToolWithOffhandKit(TestContext context) {
        ServerPlayerEntity player = createMockPlayer(context, new BlockPos(0, 1, 0));

        // Setup: damaged iron pickaxe in main hand, filled kit in offhand
        ItemStack tool = new ItemStack(Items.IRON_PICKAXE);
        int maxDamage = tool.getMaxDamage();
        tool.setDamage(maxDamage / 2); // 50% damaged
        int initialDamage = tool.getDamage();

        ItemStack kit = new ItemStack(RepairKitItems.REPAIR_KIT);
        RepairKitItem.addMaterials(kit, new Identifier("minecraft", "iron_ingot"), 10);
        int initialMaterials = RepairKitItem.getMaterialCount(kit);

        player.setStackInHand(Hand.MAIN_HAND, tool);
        player.setStackInHand(Hand.OFF_HAND, kit);

        // Perform repair
        boolean repaired = RepairKitItems.REPAIR_KIT.attemptRepair(kit, tool, player);

        context.assertTrue(repaired, "Repair should succeed");
        context.assertTrue(tool.getDamage() < initialDamage, "Tool damage should decrease");
        context.assertTrue(RepairKitItem.getMaterialCount(kit) == initialMaterials - 1,
                "Kit should have one less material");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testPlayerCannotRepairWithWrongMaterial(TestContext context) {
        ServerPlayerEntity player = createMockPlayer(context, new BlockPos(0, 1, 0));

        // Setup: diamond pickaxe with iron-filled kit
        ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
        tool.setDamage(100);
        int initialDamage = tool.getDamage();

        ItemStack kit = new ItemStack(RepairKitItems.REPAIR_KIT);
        RepairKitItem.addMaterials(kit, new Identifier("minecraft", "iron_ingot"), 10);
        int initialMaterials = RepairKitItem.getMaterialCount(kit);

        player.setStackInHand(Hand.MAIN_HAND, tool);
        player.setStackInHand(Hand.OFF_HAND, kit);

        // Attempt repair (should fail)
        boolean repaired = RepairKitItems.REPAIR_KIT.attemptRepair(kit, tool, player);

        context.assertTrue(!repaired, "Repair should fail with wrong material");
        context.assertTrue(tool.getDamage() == initialDamage, "Tool damage should be unchanged");
        context.assertTrue(RepairKitItem.getMaterialCount(kit) == initialMaterials,
                "Kit materials should be unchanged");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testPlayerCannotRepairWithEmptyKit(TestContext context) {
        ServerPlayerEntity player = createMockPlayer(context, new BlockPos(0, 1, 0));

        // Setup: damaged tool with empty kit
        ItemStack tool = new ItemStack(Items.IRON_PICKAXE);
        tool.setDamage(100);
        int initialDamage = tool.getDamage();

        ItemStack kit = new ItemStack(RepairKitItems.REPAIR_KIT);
        // Kit is empty

        player.setStackInHand(Hand.MAIN_HAND, tool);
        player.setStackInHand(Hand.OFF_HAND, kit);

        // Attempt repair (should fail)
        boolean repaired = RepairKitItems.REPAIR_KIT.attemptRepair(kit, tool, player);

        context.assertTrue(!repaired, "Repair should fail with empty kit");
        context.assertTrue(tool.getDamage() == initialDamage, "Tool damage should be unchanged");

        context.complete();
    }

    // ===== Tier-Specific Repair Tests =====

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testScrappyKitRepairs25Percent(TestContext context) {
        ServerPlayerEntity player = createMockPlayer(context, new BlockPos(0, 1, 0));

        ItemStack tool = new ItemStack(Items.IRON_PICKAXE);
        int maxDamage = tool.getMaxDamage();
        tool.setDamage(maxDamage); // Fully damaged

        ItemStack kit = new ItemStack(RepairKitItems.SCRAPPY_REPAIR_KIT);
        RepairKitItem.addMaterials(kit, new Identifier("minecraft", "iron_ingot"), 10);

        player.setStackInHand(Hand.MAIN_HAND, tool);
        player.setStackInHand(Hand.OFF_HAND, kit);

        RepairKitItems.SCRAPPY_REPAIR_KIT.attemptRepair(kit, tool, player);

        int expectedRepair = (int) (maxDamage * 0.25f);
        int actualDamage = tool.getDamage();
        int expectedDamage = maxDamage - expectedRepair;

        context.assertTrue(actualDamage == expectedDamage,
                "Scrappy kit should repair 25%. Expected damage: " + expectedDamage + ", got: " + actualDamage);

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testStandardKitRepairs50Percent(TestContext context) {
        ServerPlayerEntity player = createMockPlayer(context, new BlockPos(0, 1, 0));

        ItemStack tool = new ItemStack(Items.IRON_PICKAXE);
        int maxDamage = tool.getMaxDamage();
        tool.setDamage(maxDamage);

        ItemStack kit = new ItemStack(RepairKitItems.REPAIR_KIT);
        RepairKitItem.addMaterials(kit, new Identifier("minecraft", "iron_ingot"), 10);

        player.setStackInHand(Hand.MAIN_HAND, tool);
        player.setStackInHand(Hand.OFF_HAND, kit);

        RepairKitItems.REPAIR_KIT.attemptRepair(kit, tool, player);

        int expectedRepair = (int) (maxDamage * 0.50f);
        int actualDamage = tool.getDamage();
        int expectedDamage = maxDamage - expectedRepair;

        context.assertTrue(actualDamage == expectedDamage,
                "Standard kit should repair 50%. Expected damage: " + expectedDamage + ", got: " + actualDamage);

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testRefinedKitRepairs75Percent(TestContext context) {
        ServerPlayerEntity player = createMockPlayer(context, new BlockPos(0, 1, 0));

        ItemStack tool = new ItemStack(Items.IRON_PICKAXE);
        int maxDamage = tool.getMaxDamage();
        tool.setDamage(maxDamage);

        ItemStack kit = new ItemStack(RepairKitItems.REFINED_REPAIR_KIT);
        RepairKitItem.addMaterials(kit, new Identifier("minecraft", "iron_ingot"), 10);

        player.setStackInHand(Hand.MAIN_HAND, tool);
        player.setStackInHand(Hand.OFF_HAND, kit);

        RepairKitItems.REFINED_REPAIR_KIT.attemptRepair(kit, tool, player);

        int expectedRepair = (int) (maxDamage * 0.75f);
        int actualDamage = tool.getDamage();
        int expectedDamage = maxDamage - expectedRepair;

        context.assertTrue(actualDamage == expectedDamage,
                "Refined kit should repair 75%. Expected damage: " + expectedDamage + ", got: " + actualDamage);

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testMastercraftKitRepairs100Percent(TestContext context) {
        ServerPlayerEntity player = createMockPlayer(context, new BlockPos(0, 1, 0));

        ItemStack tool = new ItemStack(Items.IRON_PICKAXE);
        int maxDamage = tool.getMaxDamage();
        tool.setDamage(maxDamage);

        ItemStack kit = new ItemStack(RepairKitItems.MASTERCRAFT_REPAIR_KIT);
        RepairKitItem.addMaterials(kit, new Identifier("minecraft", "iron_ingot"), 10);

        player.setStackInHand(Hand.MAIN_HAND, tool);
        player.setStackInHand(Hand.OFF_HAND, kit);

        RepairKitItems.MASTERCRAFT_REPAIR_KIT.attemptRepair(kit, tool, player);

        context.assertTrue(tool.getDamage() == 0,
                "Mastercraft kit should fully repair. Got damage: " + tool.getDamage());

        context.complete();
    }

    // ===== Full Workflow Tests =====

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testFullRepairWorkflow(TestContext context) {
        ServerPlayerEntity player = createMockPlayer(context, new BlockPos(0, 1, 0));

        // Step 1: Create empty kit and materials
        ItemStack kit = new ItemStack(RepairKitItems.REPAIR_KIT);
        ItemStack materials = new ItemStack(Items.IRON_INGOT, 16);

        // Step 2: Fill the kit
        player.setStackInHand(Hand.MAIN_HAND, kit);
        player.setStackInHand(Hand.OFF_HAND, materials);

        RepairKitItems.REPAIR_KIT.use(context.getWorld(), player, Hand.MAIN_HAND);
        kit = RepairKitItems.REPAIR_KIT.finishUsing(kit, context.getWorld(), player);

        context.assertTrue(RepairKitItem.getMaterialCount(kit) == 16, "Kit should have 16 materials");

        // Step 3: Create damaged tool
        ItemStack tool = new ItemStack(Items.IRON_PICKAXE);
        tool.setDamage(tool.getMaxDamage() - 10); // Very damaged

        // Step 4: Repair the tool multiple times until full or out of materials
        int repairs = 0;
        while (tool.isDamaged() && !RepairKitItem.isEmpty(kit)) {
            RepairKitItems.REPAIR_KIT.attemptRepair(kit, tool, player);
            repairs++;
            if (repairs > 20) break; // Safety limit
        }

        context.assertTrue(!tool.isDamaged() || RepairKitItem.isEmpty(kit),
                "Tool should be repaired or kit empty. Tool damage: " + tool.getDamage() +
                        ", Kit materials: " + RepairKitItem.getMaterialCount(kit));

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testMultipleRepairsConsumeMaterials(TestContext context) {
        ServerPlayerEntity player = createMockPlayer(context, new BlockPos(0, 1, 0));

        // Use scrappy kit (25% repair) so we need multiple repairs
        ItemStack kit = new ItemStack(RepairKitItems.SCRAPPY_REPAIR_KIT);
        RepairKitItem.addMaterials(kit, new Identifier("minecraft", "iron_ingot"), 5);

        ItemStack tool = new ItemStack(Items.IRON_PICKAXE);
        int maxDamage = tool.getMaxDamage();
        tool.setDamage(maxDamage); // Fully damaged

        player.setStackInHand(Hand.MAIN_HAND, tool);
        player.setStackInHand(Hand.OFF_HAND, kit);

        // First repair
        RepairKitItems.SCRAPPY_REPAIR_KIT.attemptRepair(kit, tool, player);
        context.assertTrue(RepairKitItem.getMaterialCount(kit) == 4, "Should have 4 materials after first repair");
        context.assertTrue(tool.getDamage() < maxDamage, "Tool should be less damaged");

        // Second repair
        RepairKitItems.SCRAPPY_REPAIR_KIT.attemptRepair(kit, tool, player);
        context.assertTrue(RepairKitItem.getMaterialCount(kit) == 3, "Should have 3 materials after second repair");

        // Third repair
        RepairKitItems.SCRAPPY_REPAIR_KIT.attemptRepair(kit, tool, player);
        context.assertTrue(RepairKitItem.getMaterialCount(kit) == 2, "Should have 2 materials after third repair");

        // Fourth repair
        RepairKitItems.SCRAPPY_REPAIR_KIT.attemptRepair(kit, tool, player);
        context.assertTrue(RepairKitItem.getMaterialCount(kit) == 1, "Should have 1 material after fourth repair");

        // Fifth and final repair (tool should be fully repaired at 100% = 4 x 25%)
        RepairKitItems.SCRAPPY_REPAIR_KIT.attemptRepair(kit, tool, player);

        // Kit should be empty or tool should be fully repaired
        context.assertTrue(RepairKitItem.getMaterialCount(kit) == 0 || tool.getDamage() == 0,
                "Either kit empty or tool repaired");

        context.complete();
    }

    // ===== Armor Repair Tests =====

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testPlayerRepairsArmorWithKit(TestContext context) {
        ServerPlayerEntity player = createMockPlayer(context, new BlockPos(0, 1, 0));

        // Setup: damaged diamond chestplate with diamond-filled kit
        ItemStack armor = new ItemStack(Items.DIAMOND_CHESTPLATE);
        armor.setDamage(armor.getMaxDamage() / 2);
        int initialDamage = armor.getDamage();

        ItemStack kit = new ItemStack(RepairKitItems.REPAIR_KIT);
        RepairKitItem.addMaterials(kit, new Identifier("minecraft", "diamond"), 10);

        player.setStackInHand(Hand.MAIN_HAND, armor);
        player.setStackInHand(Hand.OFF_HAND, kit);

        boolean repaired = RepairKitItems.REPAIR_KIT.attemptRepair(kit, armor, player);

        context.assertTrue(repaired, "Armor repair should succeed");
        context.assertTrue(armor.getDamage() < initialDamage, "Armor damage should decrease");
        context.assertTrue(RepairKitItem.getMaterialCount(kit) == 9, "Kit should have one less diamond");

        context.complete();
    }

    // ===== Special Items Tests =====

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testPlayerRepairsBowWithKit(TestContext context) {
        ServerPlayerEntity player = createMockPlayer(context, new BlockPos(0, 1, 0));

        ItemStack bow = new ItemStack(Items.BOW);
        bow.setDamage(bow.getMaxDamage() / 2);
        int initialDamage = bow.getDamage();

        ItemStack kit = new ItemStack(RepairKitItems.REPAIR_KIT);
        RepairKitItem.addMaterials(kit, new Identifier("minecraft", "string"), 10);

        player.setStackInHand(Hand.MAIN_HAND, bow);
        player.setStackInHand(Hand.OFF_HAND, kit);

        boolean repaired = RepairKitItems.REPAIR_KIT.attemptRepair(kit, bow, player);

        context.assertTrue(repaired, "Bow repair should succeed");
        context.assertTrue(bow.getDamage() < initialDamage, "Bow damage should decrease");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testPlayerRepairsShieldWithKit(TestContext context) {
        ServerPlayerEntity player = createMockPlayer(context, new BlockPos(0, 1, 0));

        ItemStack shield = new ItemStack(Items.SHIELD);
        shield.setDamage(shield.getMaxDamage() / 2);
        int initialDamage = shield.getDamage();

        ItemStack kit = new ItemStack(RepairKitItems.REPAIR_KIT);
        RepairKitItem.addMaterials(kit, new Identifier("minecraft", "oak_planks"), 10);

        player.setStackInHand(Hand.MAIN_HAND, shield);
        player.setStackInHand(Hand.OFF_HAND, kit);

        boolean repaired = RepairKitItems.REPAIR_KIT.attemptRepair(kit, shield, player);

        context.assertTrue(repaired, "Shield repair should succeed with planks");
        context.assertTrue(shield.getDamage() < initialDamage, "Shield damage should decrease");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testPlayerRepairsElytraWithKit(TestContext context) {
        ServerPlayerEntity player = createMockPlayer(context, new BlockPos(0, 1, 0));

        ItemStack elytra = new ItemStack(Items.ELYTRA);
        elytra.setDamage(elytra.getMaxDamage() / 2);
        int initialDamage = elytra.getDamage();

        ItemStack kit = new ItemStack(RepairKitItems.REPAIR_KIT);
        RepairKitItem.addMaterials(kit, new Identifier("minecraft", "phantom_membrane"), 10);

        player.setStackInHand(Hand.MAIN_HAND, elytra);
        player.setStackInHand(Hand.OFF_HAND, kit);

        boolean repaired = RepairKitItems.REPAIR_KIT.attemptRepair(kit, elytra, player);

        context.assertTrue(repaired, "Elytra repair should succeed with phantom membrane");
        context.assertTrue(elytra.getDamage() < initialDamage, "Elytra damage should decrease");

        context.complete();
    }
}
