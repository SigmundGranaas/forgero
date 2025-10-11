package com.sigmundgranaas.forgero.properties.gametest;

import com.sigmundgranaas.forgero.properties.minecraft.loot.LootProperty;
import com.sigmundgranaas.forgero.properties.minecraft.loot.filter.IsItemFilter;
import com.sigmundgranaas.forgero.properties.minecraft.loot.filter.ItemFilter;
import com.sigmundgranaas.forgero.properties.minecraft.loot.filter.TagFilter;
import com.sigmundgranaas.forgero.properties.minecraft.loot.function.AutoSmeltFunction;
import com.sigmundgranaas.forgero.properties.minecraft.loot.function.ItemTransformFunction;
import com.sigmundgranaas.forgero.properties.minecraft.loot.handler.ApplyFunctionsHandler;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.List;
import java.util.Set;

public class LootGametest {
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testTagFilter(TestContext context) {
		TagFilter logFilter = new TagFilter(new Identifier("minecraft", "logs"));
		ItemStack oakLog = new ItemStack(Items.OAK_LOG);
		ItemStack stone = new ItemStack(Items.STONE);

		context.assertTrue(logFilter.test(oakLog), "TagFilter should return true for an item in the tag.");
		context.assertFalse(logFilter.test(stone), "TagFilter should return false for an item not in the tag.");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testAutoSmeltFunction(TestContext context) {
		ItemFilter smeltableFilter = new TagFilter(new Identifier("forgero", "smeltable_ores"));
		AutoSmeltFunction smelter = new AutoSmeltFunction(smeltableFilter);
		ItemStack rawIron = new ItemStack(Items.RAW_IRON);

		LootContextParameterSet parameterSet = new LootContextParameterSet.Builder(context.getWorld()).build(LootContextTypes.EMPTY);
		LootContext lootContext = new LootContext.Builder(parameterSet).build(null);

		ItemStack result = smelter.apply(rawIron, lootContext);

		context.assertTrue(result.isOf(Items.IRON_INGOT), "AutoSmeltFunction should turn raw iron into an iron ingot.");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testItemTransformFunction(TestContext context) {
		ItemFilter diamondFilter = new TagFilter(new Identifier("c", "diamonds")); // Using common tag
		ItemTransformFunction transformer = new ItemTransformFunction(diamondFilter, new Identifier("minecraft", "stick"), 5);
		ItemStack diamond = new ItemStack(Items.DIAMOND);

		LootContextParameterSet parameterSet = new LootContextParameterSet.Builder(context.getWorld()).build(LootContextTypes.EMPTY);
		LootContext lootContext = new LootContext.Builder(parameterSet).build(null);

		ItemStack result = transformer.apply(diamond, lootContext);

		context.assertTrue(result.isOf(Items.STICK), "ItemTransformFunction should turn a diamond into a stick.");
		context.assertTrue(result.getCount() == 5, "ItemTransformFunction should produce 5 sticks, but got " + result.getCount());
		context.complete();
	}


	public void testBlockLootMixinWithAutoSmelt(TestContext context) {
		// 1. Create a dynamic pickaxe with auto-smelt property
		var filter = new TagFilter(new Identifier("forgero", "smeltable_ores"));
		var smeltFunction = new AutoSmeltFunction(filter);
		var handler = new ApplyFunctionsHandler(List.of(smeltFunction));
		var property = new LootProperty(handler, null);

		ItemStack pickaxe = ComponentTester.createStack("autosmelt_pick", Set.of("pickaxe", "tool"), List.of(property));
		context.assertTrue(!pickaxe.isEmpty(), "Failed to create dynamic pickaxe");

		// 2. Setup world
		BlockPos orePos = new BlockPos(1, 1, 1);
		context.setBlockState(orePos, Blocks.IRON_ORE);

		// 3. Simulate mining
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.setStackInHand(Hand.MAIN_HAND, pickaxe);
		PlayerActionTestHelper actionHelper = new PlayerActionTestHelper(context, player);
		actionHelper.mineBlock(context.getAbsolutePos(orePos));

		// 4. Assert outcome
		context.waitAndRun(5, () -> {
			// Assert that the block was actually broken
			context.expectBlock(Blocks.AIR, orePos);

			// Assert that the correct item was dropped
			List<ItemEntity> items = context.getWorld().getEntitiesByClass(
					ItemEntity.class,
					new Box(context.getAbsolutePos(orePos)).expand(2),
					(entity) -> entity.getStack().isOf(Items.IRON_INGOT)
			);

			context.assertTrue(!items.isEmpty(), "Expected to find a dropped Iron Ingot, but found none.");
			context.complete();
		});
	}


	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testEntityLootMixinWithItemTransform(TestContext context) {
		// 1. Create a dynamic sword that turns porkchops into cooked porkchops
		// Use the new, precise IsItemFilter instead of a non-existent tag
		var filter = new IsItemFilter(new Identifier("minecraft", "porkchop"));
		var cookFunction = new ItemTransformFunction(filter, new Identifier("minecraft", "cooked_porkchop"), 1);
		var handler = new ApplyFunctionsHandler(List.of(cookFunction));
		var property = new LootProperty(handler, null);

		ItemStack sword = ComponentTester.createStack("chef_sword", Set.of("sword", "tool"), List.of(property));
		context.assertTrue(!sword.isEmpty(), "Failed to create dynamic sword");

		// 2. Setup entities
		PigEntity pig = context.spawnEntity(EntityType.PIG, new BlockPos(1, 1, 1));
		BlockPos deathPos = pig.getBlockPos();
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.setStackInHand(Hand.MAIN_HAND, sword);

		// 3. Kill the entity
		PlayerActionTestHelper actionHelper = new PlayerActionTestHelper(context, player);
		actionHelper.killEntity(pig);

		// 4. Assert the loot drops
		context.waitAndRun(5, () -> {
			List<ItemEntity> items = context.getWorld().getEntitiesByClass(ItemEntity.class, new Box(deathPos).expand(3), item -> true);

			boolean foundCooked = false;
			for (ItemEntity itemEntity : items) {
				ItemStack stack = itemEntity.getStack();
				if (stack.isOf(Items.PORKCHOP)) {
					context.throwGameTestException("Found raw porkchop, but it should have been transformed.");
				}
				if (stack.isOf(Items.COOKED_PORKCHOP)) {
					foundCooked = true;
				}
			}

			context.assertTrue(foundCooked, "Expected to find cooked porkchop loot, but found none.");
			context.complete();
		});
	}
}
