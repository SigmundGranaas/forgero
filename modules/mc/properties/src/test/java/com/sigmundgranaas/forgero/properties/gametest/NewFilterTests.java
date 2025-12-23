package com.sigmundgranaas.forgero.properties.gametest;

import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.filter.BlockTagFilter;
import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.EnvironmentFilter;
import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.EntityStateFilter;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

/**
 * Comprehensive gametests for all new filter types:
 * - EnvironmentFilter (EntityFilter)
 * - EntityStateFilter (EntityFilter)
 * - BlockTagFilter (BlockFilter)
 */
public class NewFilterTests {

	// ========== EnvironmentFilter Tests ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testEnvironmentFilterLightLevel(TestContext context) {
		// Create a COMPLETELY enclosed 3x3x3 dark room
		BlockPos centerRelative = new BlockPos(2, 1, 2);

		// Floor
		for (int x = 1; x <= 3; x++) {
			for (int z = 1; z <= 3; z++) {
				context.setBlockState(new BlockPos(x, 0, z), Blocks.STONE);
			}
		}

		// Walls (all 4 sides)
		for (int y = 1; y <= 2; y++) {
			// North wall (z=1)
			for (int x = 1; x <= 3; x++) {
				context.setBlockState(new BlockPos(x, y, 1), Blocks.STONE);
			}
			// South wall (z=3)
			for (int x = 1; x <= 3; x++) {
				context.setBlockState(new BlockPos(x, y, 3), Blocks.STONE);
			}
			// West wall (x=1)
			for (int z = 1; z <= 3; z++) {
				context.setBlockState(new BlockPos(1, y, z), Blocks.STONE);
			}
			// East wall (x=3)
			for (int z = 1; z <= 3; z++) {
				context.setBlockState(new BlockPos(3, y, z), Blocks.STONE);
			}
		}

		// Ceiling (solid roof)
		for (int x = 1; x <= 3; x++) {
			for (int z = 1; z <= 3; z++) {
				context.setBlockState(new BlockPos(x, 3, z), Blocks.STONE);
			}
		}

		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		LivingEntity targetInDark = context.spawnEntity(EntityType.ZOMBIE, centerRelative);

		context.waitAndRun(5, () -> {
			// Filter for dark areas (light level 0-7)
			EnvironmentFilter darkFilter = new EnvironmentFilter(
					Optional.empty(),
					Optional.of(0),
					Optional.of(7),
					EnvironmentFilter.LightSourceType.COMBINED,
					Optional.empty()
			);

			boolean result = darkFilter.test(source, targetInDark);
			int lightLevel = targetInDark.getWorld().getLightLevel(targetInDark.getBlockPos());

			context.assertTrue(lightLevel <= 7, "Light level should be low (dark area), was " + lightLevel);
			context.assertTrue(result, "Filter should accept entity in dark area");
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testEnvironmentFilterLightLevelBright(TestContext context) {
		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));

		// Use RELATIVE coordinates - place torch for bright area
		BlockPos brightPosRelative = new BlockPos(3, 1, 3);
		context.setBlockState(brightPosRelative.down(), Blocks.STONE); // Ground
		context.setBlockState(brightPosRelative.up(), Blocks.TORCH);
		LivingEntity targetInBright = context.spawnEntity(EntityType.ZOMBIE, brightPosRelative);

		context.waitAndRun(2, () -> {
			// Filter for bright areas (light level 8+)
			EnvironmentFilter brightFilter = new EnvironmentFilter(
					Optional.empty(),
					Optional.of(8),
					Optional.of(15),
					EnvironmentFilter.LightSourceType.COMBINED,
					Optional.empty()
			);

			int lightLevel = targetInBright.getWorld().getLightLevel(targetInBright.getBlockPos());
			boolean result = brightFilter.test(source, targetInBright);

			context.assertTrue(lightLevel >= 8, "Light level should be high (bright area), was " + lightLevel);
			context.assertTrue(result, "Filter should accept entity in bright area");
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testEnvironmentFilterRaining(TestContext context) {
		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 2));

		// Filter that requires rain
		EnvironmentFilter rainFilter = new EnvironmentFilter(
				Optional.empty(),
				Optional.empty(),
				Optional.empty(),
				EnvironmentFilter.LightSourceType.COMBINED,
				Optional.of(true)
		);

		boolean isRaining = target.getWorld().isRaining();
		boolean result = rainFilter.test(source, target);

		// Result depends on weather (usually not raining in test world)
		if (isRaining) {
			context.assertTrue(result, "Filter should accept entity when raining");
		} else {
			context.assertFalse(result, "Filter should reject entity when not raining");
		}

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testEnvironmentFilterAllOptionalEmpty(TestContext context) {
		// Filter with all optional fields empty - should always pass
		EnvironmentFilter emptyFilter = new EnvironmentFilter(
				Optional.empty(),
				Optional.empty(),
				Optional.empty(),
				EnvironmentFilter.LightSourceType.COMBINED,
				Optional.empty()
		);

		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 2));

		boolean result = emptyFilter.test(source, target);

		context.assertTrue(result, "Empty filter should always pass");
		context.complete();
	}

	// ========== EntityStateFilter Tests ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testEntityStateFilterBurning(TestContext context) {
		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		LivingEntity burningTarget = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 2));
		LivingEntity normalTarget = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(4, 1, 4));

		burningTarget.setOnFireFor(5);

		// Filter that requires burning
		EntityStateFilter burningFilter = new EntityStateFilter(
				Optional.of(true),
				Optional.empty(),
				Optional.empty(),
				Optional.empty()
		);

		context.waitAndRun(1, () -> {
			boolean burningResult = burningFilter.test(source, burningTarget);
			boolean normalResult = burningFilter.test(source, normalTarget);

			context.assertTrue(burningTarget.isOnFire(), "Target should be on fire");
			context.assertTrue(burningResult, "Filter should accept burning entity");
			context.assertFalse(normalResult, "Filter should reject non-burning entity");
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testEntityStateFilterSprinting(TestContext context) {
		PlayerEntity source = context.createMockCreativeServerPlayerInWorld();
		PlayerEntity sprintingTarget = context.createMockCreativeServerPlayerInWorld();

		sprintingTarget.setSprinting(true);

		// Filter that requires sprinting
		EntityStateFilter sprintingFilter = new EntityStateFilter(
				Optional.empty(),
				Optional.of(true),
				Optional.empty(),
				Optional.empty()
		);

		boolean result = sprintingFilter.test(source, sprintingTarget);

		context.assertTrue(sprintingTarget.isSprinting(), "Target should be sprinting");
		context.assertTrue(result, "Filter should accept sprinting entity");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testEntityStateFilterAirborne(TestContext context) {
		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		// Use RELATIVE coordinates - place a block for grounded entity
		BlockPos groundedPosRelative = new BlockPos(2, 2, 2);
		context.setBlockState(groundedPosRelative.down(), Blocks.STONE);
		LivingEntity groundedTarget = context.spawnEntity(EntityType.ZOMBIE, groundedPosRelative);

		// Spawn airborne entity higher up with no block below (RELATIVE)
		LivingEntity airborneTarget = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(4, 3, 4));

		// Filter that requires airborne
		EntityStateFilter airborneFilter = new EntityStateFilter(
				Optional.empty(),
				Optional.empty(),
				Optional.of(true),
				Optional.empty()
		);

		context.waitAndRun(3, () -> {
			boolean groundedResult = airborneFilter.test(source, groundedTarget);
			boolean airborneResult = airborneFilter.test(source, airborneTarget);

			context.assertTrue(groundedTarget.isOnGround(), "Grounded target should be on ground");
			context.assertFalse(groundedResult, "Filter should reject grounded entity");

			if (!airborneTarget.isOnGround()) {
				context.assertTrue(airborneResult, "Filter should accept airborne entity");
			}

			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testEntityStateFilterHealthPercentage(TestContext context) {
		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		LivingEntity lowHealthTarget = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 2));
		LivingEntity fullHealthTarget = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(4, 1, 4));

		// Set low health (30% of max)
		lowHealthTarget.setHealth(lowHealthTarget.getMaxHealth() * 0.3f);

		// Filter for entities with <= 50% health
		EntityStateFilter lowHealthFilter = new EntityStateFilter(
				Optional.empty(),
				Optional.empty(),
				Optional.empty(),
				Optional.of(0.5)
		);

		boolean lowHealthResult = lowHealthFilter.test(source, lowHealthTarget);
		boolean fullHealthResult = lowHealthFilter.test(source, fullHealthTarget);

		context.assertTrue(lowHealthResult, "Filter should accept low-health entity");
		context.assertFalse(fullHealthResult, "Filter should reject full-health entity");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testEntityStateFilterCombinedConditions(TestContext context) {
		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 2));

		target.setOnFireFor(5);
		target.setHealth(target.getMaxHealth() * 0.2f);

		// Filter requiring both burning AND low health
		EntityStateFilter combinedFilter = new EntityStateFilter(
				Optional.of(true), // Must be burning
				Optional.empty(),
				Optional.empty(),
				Optional.of(0.5)   // Must have <= 50% health
		);

		context.waitAndRun(1, () -> {
			boolean result = combinedFilter.test(source, target);

			context.assertTrue(target.isOnFire(), "Target should be burning");
			context.assertTrue(target.getHealth() / target.getMaxHealth() <= 0.5, "Target should have low health");
			context.assertTrue(result, "Filter should accept entity meeting all conditions");
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testEntityStateFilterNegativeConditions(TestContext context) {
		PlayerEntity source = context.createMockCreativeServerPlayerInWorld();
		PlayerEntity target = context.createMockCreativeServerPlayerInWorld();

		target.setSprinting(false);

		// Filter requiring NOT sprinting
		EntityStateFilter notSprintingFilter = new EntityStateFilter(
				Optional.empty(),
				Optional.of(false),
				Optional.empty(),
				Optional.empty()
		);

		boolean result = notSprintingFilter.test(source, target);

		context.assertFalse(target.isSprinting(), "Target should not be sprinting");
		context.assertTrue(result, "Filter should accept non-sprinting entity");
		context.complete();
	}

	// ========== BlockTagFilter Tests ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testBlockTagFilterLogsTag(TestContext context) {
		// Use RELATIVE coordinates for block placement
		BlockPos logPosRelative = new BlockPos(1, 1, 1);
		context.setBlockState(logPosRelative, Blocks.OAK_LOG);

		BlockPos stonePosRelative = new BlockPos(2, 1, 1);
		context.setBlockState(stonePosRelative, Blocks.STONE);

		// Convert to ABSOLUTE coordinates
		BlockPos logPos = context.getAbsolutePos(logPosRelative);
		BlockPos stonePos = context.getAbsolutePos(stonePosRelative);

		LivingEntity entity = context.spawnEntity(EntityType.ZOMBIE, logPosRelative);

		// Verify blocks are actually placed using absolute positions
		BlockState logState = context.getWorld().getBlockState(logPos);
		BlockState stoneState = context.getWorld().getBlockState(stonePos);

		context.assertTrue(logState.getBlock() == Blocks.OAK_LOG, "Log block should be oak log, was " + logState.getBlock());
		context.assertTrue(stoneState.getBlock() == Blocks.STONE, "Stone block should be stone, was " + stoneState.getBlock());

		// Filter for logs tag using ABSOLUTE coordinates
		BlockTagFilter logsFilter = new BlockTagFilter("minecraft:logs");

		boolean logResult = logsFilter.filter(entity, logPos, logPos);
		boolean stoneResult = logsFilter.filter(entity, stonePos, stonePos);

		// Check if block is actually in the tag
		boolean isInTag = logState.isIn(TagKey.of(RegistryKeys.BLOCK, new Identifier("minecraft:logs")));
		context.assertTrue(isInTag, "Oak log should be in minecraft:logs tag");

		context.assertTrue(logResult, "Filter should accept oak log (in logs tag)");
		context.assertFalse(stoneResult, "Filter should reject stone (not in logs tag)");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testBlockTagFilterPlanksTag(TestContext context) {
		// Use RELATIVE coordinates
		BlockPos planksPosRelative = new BlockPos(1, 1, 1);
		context.setBlockState(planksPosRelative, Blocks.OAK_PLANKS);

		BlockPos dirtPosRelative = new BlockPos(2, 1, 1);
		context.setBlockState(dirtPosRelative, Blocks.DIRT);

		// Convert to ABSOLUTE
		BlockPos planksPos = context.getAbsolutePos(planksPosRelative);
		BlockPos dirtPos = context.getAbsolutePos(dirtPosRelative);

		LivingEntity entity = context.spawnEntity(EntityType.ZOMBIE, planksPosRelative);

		// Filter for planks tag
		BlockTagFilter planksFilter = new BlockTagFilter("minecraft:planks");

		boolean planksResult = planksFilter.filter(entity, planksPos, planksPos);
		boolean dirtResult = planksFilter.filter(entity, dirtPos, dirtPos);

		context.assertTrue(planksResult, "Filter should accept oak planks");
		context.assertFalse(dirtResult, "Filter should reject dirt");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testBlockTagFilterOresTag(TestContext context) {
		// Use RELATIVE coordinates
		BlockPos orePosRelative = new BlockPos(1, 1, 1);
		context.setBlockState(orePosRelative, Blocks.IRON_ORE);

		BlockPos cobblePosRelative = new BlockPos(2, 1, 1);
		context.setBlockState(cobblePosRelative, Blocks.COBBLESTONE);

		// Convert to ABSOLUTE
		BlockPos orePos = context.getAbsolutePos(orePosRelative);
		BlockPos cobblePos = context.getAbsolutePos(cobblePosRelative);

		LivingEntity entity = context.spawnEntity(EntityType.ZOMBIE, orePosRelative);

		// Filter for ores tag
		BlockTagFilter oresFilter = new BlockTagFilter("minecraft:iron_ores");

		boolean oreResult = oresFilter.filter(entity, orePos, orePos);
		boolean cobbleResult = oresFilter.filter(entity, cobblePos, cobblePos);

		context.assertTrue(oreResult, "Filter should accept iron ore");
		context.assertFalse(cobbleResult, "Filter should reject cobblestone");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testBlockTagFilterInvalidTag(TestContext context) {
		// Use RELATIVE coordinates
		BlockPos posRelative = new BlockPos(1, 1, 1);
		context.setBlockState(posRelative, Blocks.STONE);

		// Convert to ABSOLUTE
		BlockPos pos = context.getAbsolutePos(posRelative);

		LivingEntity entity = context.spawnEntity(EntityType.ZOMBIE, posRelative);

		// Filter with non-existent tag
		BlockTagFilter invalidFilter = new BlockTagFilter("minecraft:nonexistent_tag");

		boolean result = invalidFilter.filter(entity, pos, pos);

		// Should not crash, should return false
		context.assertFalse(result, "Filter with invalid tag should reject all blocks");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testBlockTagFilterMultipleBlocks(TestContext context) {
		// Use RELATIVE coordinates
		BlockPos oakLogPosRelative = new BlockPos(1, 1, 1);
		context.setBlockState(oakLogPosRelative, Blocks.OAK_LOG);

		BlockPos birchLogPosRelative = new BlockPos(2, 1, 1);
		context.setBlockState(birchLogPosRelative, Blocks.BIRCH_LOG);

		BlockPos spruceLogPosRelative = new BlockPos(3, 1, 1);
		context.setBlockState(spruceLogPosRelative, Blocks.SPRUCE_LOG);

		// Convert to ABSOLUTE
		BlockPos oakLogPos = context.getAbsolutePos(oakLogPosRelative);
		BlockPos birchLogPos = context.getAbsolutePos(birchLogPosRelative);
		BlockPos spruceLogPos = context.getAbsolutePos(spruceLogPosRelative);

		LivingEntity entity = context.spawnEntity(EntityType.ZOMBIE, oakLogPosRelative);

		// Filter for logs tag
		BlockTagFilter logsFilter = new BlockTagFilter("minecraft:logs");

		boolean oakResult = logsFilter.filter(entity, oakLogPos, oakLogPos);
		boolean birchResult = logsFilter.filter(entity, birchLogPos, birchLogPos);
		boolean spruceResult = logsFilter.filter(entity, spruceLogPos, spruceLogPos);

		context.assertTrue(oakResult, "Filter should accept oak log");
		context.assertTrue(birchResult, "Filter should accept birch log");
		context.assertTrue(spruceResult, "Filter should accept spruce log");
		context.complete();
	}
}
