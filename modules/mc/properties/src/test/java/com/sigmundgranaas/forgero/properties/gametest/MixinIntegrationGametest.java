package com.sigmundgranaas.forgero.properties.gametest;

import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.BlockBreakingProperty;
import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.filter.FilterWrapper;
import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.filter.SameBlockFilter;
import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.hardness.Instant;
import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.selector.PatternSelector;
import com.sigmundgranaas.forgero.properties.minecraft.onhit.OnHitProperty;
import com.sigmundgranaas.forgero.effects.entity.FireHandler;
import com.sigmundgranaas.forgero.properties.minecraft.entityselector.SingleTargetSelector;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Set;


public class MixinIntegrationGametest {

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void test3x3PatternMining(TestContext context) {
		// 1. Create the dynamic item with an instant-break property
		var filter = new FilterWrapper(List.of(new SameBlockFilter()));
		var pattern = new PatternSelector(List.of("xxx", "xcx", "xxx"), 1, "multi", filter);
		var speed = new Instant(false);
		var pickaxeProperty = new BlockBreakingProperty(pattern, speed, null);

		ItemStack pickaxeStack = ComponentTester.createStack(
				"test_pickaxe_3x3",
				Set.of("pickaxe", "tool"),
				List.of(pickaxeProperty)
		);
		context.assertTrue(!pickaxeStack.isEmpty(), "Failed to create dynamic pickaxe stack");


		// 2. Setup world state
		BlockPos centerRelative = new BlockPos(1, 1, 1);
		BlockPos centerAbsolute = context.getAbsolutePos(centerRelative);
		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				context.setBlockState(centerRelative.add(x, 0, z), Blocks.IRON_ORE);
			}
		}

		// 3. Execute the action using the correct player simulation
		// The player needs to be close enough to mine the block
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.teleport(centerAbsolute.getX(), centerAbsolute.getY(), centerAbsolute.getZ());
		player.setStackInHand(Hand.MAIN_HAND, pickaxeStack);
		player.setYaw(0); // Face South to ensure deterministic rotation
		player.setPitch(45); // Look down at the block, this is crucial for the pattern selector

		PlayerActionTestHelper actionHelper = new PlayerActionTestHelper(context, player);
		actionHelper.mineBlock(centerAbsolute);

		// 4. Assert the outcome
		context.waitAndRun(5, () -> {
			for (int x = -1; x <= 1; x++) {
				for (int z = -1; z <= 1; z++) {
					context.expectBlock(Blocks.AIR, centerRelative.add(x, 0, z));
				}
			}
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testOnHitFireEffect(TestContext context) {
		// 1. Create the dynamic item using the new OnHitProperty structure
		var fireEffect = new FireHandler(5);
		var selector = new SingleTargetSelector(List.of());
		var swordProperty = new OnHitProperty(selector, List.of(fireEffect), null);

		ItemStack swordStack = ComponentTester.createStack(
				"test_sword_fire",
				Set.of("sword"),
				List.of(swordProperty)
		);
		context.assertTrue(!swordStack.isEmpty(), "Failed to create dynamic sword stack");

		// 2. Setup entities
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.setStackInHand(Hand.MAIN_HAND, swordStack);
		LivingEntity target = context.spawnEntity(EntityType.PIG, new BlockPos(1, 1, 1));

		// 3. Perform the action
		player.attack(target);

		// 4. Assert the outcome
		context.waitAndRun(5, () -> {
			context.assertTrue(target.isOnFire(), "Target should be on fire after being hit.");
			context.complete();
		});
	}
}
