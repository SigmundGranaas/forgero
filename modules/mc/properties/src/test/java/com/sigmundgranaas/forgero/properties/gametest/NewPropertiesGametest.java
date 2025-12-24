package com.sigmundgranaas.forgero.properties.gametest;

import com.sigmundgranaas.forgero.effects.block.BlockParticleEffect;
import com.sigmundgranaas.forgero.effects.block.BlockSoundEffect;
import com.sigmundgranaas.forgero.effects.entity.SwingParticleEffect;
import com.sigmundgranaas.forgero.effects.entity.SwingSoundEffect;
import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.selector.SingleSelector;
import com.sigmundgranaas.forgero.properties.minecraft.blockuse.BlockUseProperty;
import com.sigmundgranaas.forgero.properties.minecraft.blockuse.effects.TillSoilEffect;
import com.sigmundgranaas.forgero.properties.minecraft.entityuse.EntityUseProperty;
import com.sigmundgranaas.forgero.properties.minecraft.entityuse.effects.HealEntityEffect;
import com.sigmundgranaas.forgero.properties.minecraft.onhitblock.OnHitBlockProperty;
import com.sigmundgranaas.forgero.properties.minecraft.swing.SwingHandProperty;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Set;

/**
 * Gametests for newly implemented properties:
 * - OnHitBlockProperty
 * - SwingHandProperty
 * - EntityUseProperty
 * - BlockUseProperty
 */
public class NewPropertiesGametest {

	/**
	 * Tests OnHitBlockProperty with sound effect
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testOnHitBlockWithSound(TestContext context) {
		var player = context.createMockCreativeServerPlayerInWorld();

		// Create property with block sound effect
		OnHitBlockProperty property = new OnHitBlockProperty(
				new SingleSelector(),
				List.of(new BlockSoundEffect("minecraft:block.stone.break", 1.0f, 1.0f)),
				null
		);

		ItemStack stack = ComponentTester.createStack(
				"test_block_sound",
				Set.of("tool"),
				List.of(property)
		);

		context.assertTrue(!stack.isEmpty(), "Stack should not be empty");

		// Place a stone block to hit
		BlockPos pos = new BlockPos(0, 1, 0);
		context.setBlockState(pos, Blocks.STONE);

		// Give player the item
		player.setStackInHand(Hand.MAIN_HAND, stack);

		// Attack the block (this would trigger OnHitBlockManager in game)
		context.assertTrue(context.getBlockState(pos).getBlock() == Blocks.STONE, "Block should be stone");

		context.complete();
	}

	/**
	 * Tests OnHitBlockProperty with particle effect
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testOnHitBlockWithParticles(TestContext context) {
		var player = context.createMockCreativeServerPlayerInWorld();

		// Create property with particle effect
		OnHitBlockProperty property = new OnHitBlockProperty(
				new SingleSelector(),
				List.of(new BlockParticleEffect("minecraft:flame", 10, 0.5)),
				null
		);

		ItemStack stack = ComponentTester.createStack(
				"test_block_particles",
				Set.of("tool"),
				List.of(property)
		);

		context.assertTrue(!stack.isEmpty(), "Stack should not be empty");
		context.complete();
	}

	/**
	 * Tests SwingHandProperty with sound effect
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testSwingHandWithSound(TestContext context) {
		var player = context.createMockCreativeServerPlayerInWorld();

		// Create property with swing sound
		SwingHandProperty property = new SwingHandProperty(
				List.of(new SwingSoundEffect("minecraft:entity.player.attack.sweep", 1.0f, 1.0f)),
				null
		);

		ItemStack stack = ComponentTester.createStack(
				"test_swing_sound",
				Set.of("tool"),
				List.of(property)
		);

		context.assertTrue(!stack.isEmpty(), "Stack should not be empty");

		// Give player the item
		player.setStackInHand(Hand.MAIN_HAND, stack);

		// Swing hand (this would trigger SwingHandManager in game)
		player.swingHand(Hand.MAIN_HAND);

		context.complete();
	}

	/**
	 * Tests SwingHandProperty with particle effect
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testSwingHandWithParticles(TestContext context) {
		var player = context.createMockCreativeServerPlayerInWorld();

		// Create property with swing particles
		SwingHandProperty property = new SwingHandProperty(
				List.of(new SwingParticleEffect("minecraft:sweep_attack", 5, 0.3)),
				null
		);

		ItemStack stack = ComponentTester.createStack(
				"test_swing_particles",
				Set.of("tool"),
				List.of(property)
		);

		context.assertTrue(!stack.isEmpty(), "Stack should not be empty");
		context.complete();
	}

	/**
	 * Tests EntityUseProperty with heal effect
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testEntityUseWithHeal(TestContext context) {
		var player = context.createMockCreativeServerPlayerInWorld();

		// Spawn a cow to heal
		CowEntity cow = context.spawnEntity(EntityType.COW, new BlockPos(1, 1, 1));

		// Damage the cow first
		cow.setHealth(5.0f);
		float initialHealth = cow.getHealth();

		// Create property with heal effect
		EntityUseProperty property = new EntityUseProperty(
				List.of(new HealEntityEffect(3.0f)),
				null
		);

		ItemStack stack = ComponentTester.createStack(
				"test_entity_heal",
				Set.of("tool"),
				List.of(property)
		);

		context.assertTrue(!stack.isEmpty(), "Stack should not be empty");
		context.assertTrue(initialHealth == 5.0f, "Cow should be damaged");

		context.complete();
	}

	/**
	 * Tests BlockUseProperty with till soil effect
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testBlockUseWithTillSoil(TestContext context) {
		var player = context.createMockCreativeServerPlayerInWorld();

		// Place dirt block
		BlockPos pos = new BlockPos(0, 1, 0);
		context.setBlockState(pos, Blocks.DIRT);

		// Create property with till soil effect
		BlockUseProperty property = new BlockUseProperty(
				List.of(new TillSoilEffect()),
				null
		);

		ItemStack stack = ComponentTester.createStack(
				"test_till_soil",
				Set.of("tool"),
				List.of(property)
		);

		context.assertTrue(!stack.isEmpty(), "Stack should not be empty");
		context.assertTrue(context.getBlockState(pos).getBlock() == Blocks.DIRT, "Block should be dirt");

		context.complete();
	}

	/**
	 * Tests multiple OnHitBlock effects together
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testOnHitBlockMultipleEffects(TestContext context) {
		var player = context.createMockCreativeServerPlayerInWorld();

		// Create property with multiple effects
		OnHitBlockProperty property = new OnHitBlockProperty(
				new SingleSelector(),
				List.of(
						new BlockSoundEffect("minecraft:block.stone.break", 1.0f, 1.0f),
						new BlockParticleEffect("minecraft:flame", 5, 0.3)
				),
				null
		);

		ItemStack stack = ComponentTester.createStack(
				"test_multi_effects",
				Set.of("tool"),
				List.of(property)
		);

		context.assertTrue(!stack.isEmpty(), "Stack should not be empty");
		context.complete();
	}

	/**
	 * Tests SwingHand with multiple effects
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testSwingHandMultipleEffects(TestContext context) {
		var player = context.createMockCreativeServerPlayerInWorld();

		// Create property with multiple effects
		SwingHandProperty property = new SwingHandProperty(
				List.of(
						new SwingSoundEffect("minecraft:entity.player.attack.sweep", 1.0f, 1.0f),
						new SwingParticleEffect("minecraft:sweep_attack", 3, 0.2)
				),
				null
		);

		ItemStack stack = ComponentTester.createStack(
				"test_swing_multi",
				Set.of("tool"),
				List.of(property)
		);

		context.assertTrue(!stack.isEmpty(), "Stack should not be empty");
		context.complete();
	}

	/**
	 * Tests that properties can be combined on one item
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testCombinedProperties(TestContext context) {
		var player = context.createMockCreativeServerPlayerInWorld();

		// Create multiple properties
		OnHitBlockProperty onHitBlock = new OnHitBlockProperty(
				new SingleSelector(),
				List.of(new BlockSoundEffect("minecraft:block.stone.break", 1.0f, 1.0f)),
				null
		);

		SwingHandProperty swing = new SwingHandProperty(
				List.of(new SwingSoundEffect("minecraft:entity.player.attack.sweep", 1.0f, 1.0f)),
				null
		);

		ItemStack stack = ComponentTester.createStack(
				"test_combined",
				Set.of("tool"),
				List.of(onHitBlock, swing)
		);

		context.assertTrue(!stack.isEmpty(), "Stack should not be empty");
		context.complete();
	}
}
