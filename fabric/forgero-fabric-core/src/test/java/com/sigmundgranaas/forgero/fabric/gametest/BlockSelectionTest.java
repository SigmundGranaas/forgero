package com.sigmundgranaas.forgero.fabric.gametest;

import static com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.selector.PatternSelector.DEPTH_MODIFIER;
import static com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.selector.PatternSelector.multiDirection;
import static com.sigmundgranaas.forgero.minecraft.common.toolhandler.block.BlockUtils.isBreakableBlock;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import com.sigmundgranaas.forgero.minecraft.common.feature.ModifiableFeatureAttribute;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.filter.BlockFilter;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.selector.BlockSelector;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.selector.PatternSelector;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.selector.RadiusVeinSelector;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;

@SuppressWarnings("unused")
public class BlockSelectionTest {

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "BlockSelectionTest")
	public void test3x3PatternSelectionNorth(TestContext context) {
		BlockPos rootPos = context.getAbsolutePos(new BlockPos(0, 0, 0)).add(-3, 1, -3);
		BlockPos relative = context.getRelativePos(rootPos);

		Set<BlockPos> square = insert(createSquare(relative, 3, 3, 1), context);

		BlockPos centerOffset = new BlockPos(1, 1, 0);
		BlockSelector selector = selector(pattern3x3());
		Set<BlockPos> selected = selector.select(relative.add(centerOffset), context.createMockCreativePlayer());

		assertSelected(selected, square, context);
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "BlockSelectionTest")
	public void test3x3PatternSelectionEast(TestContext context) {
		BlockPos rootPos = context.getAbsolutePos(new BlockPos(0, 0, 0)).add(-3, 1, -3);
		BlockPos relative = context.getRelativePos(rootPos);

		Set<BlockPos> square = insert(createSquare(relative, 3, 1, 3), context);

		BlockSelector selector = selector(pattern3x3());

		PlayerEntity entity = context.createMockCreativePlayer();
		BlockPos center = context.getAbsolutePos(relative.add(0, 1, 1));
		entity.teleport(center.getX(), center.getY(), center.getZ() + 1, false);
		entity.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, new Vec3d(center.getX(), center.getY(), center.getZ()));

		Set<BlockPos> selected = selector.select(relative.add(0, 1, 1), entity);

		assertSelected(selected, square, context);
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "BlockSelectionTest")
	public void test3x3PatternSelectionDown(TestContext context) {
		BlockPos rootPos = context.getAbsolutePos(new BlockPos(0, 0, 0)).add(-3, 1, -3);
		BlockPos relative = context.getRelativePos(rootPos);

		Set<BlockPos> square = insert(createSquare(relative, 1, 3, 3), context);

		BlockSelector selector = selector(pattern3x3());
		PlayerEntity entity = context.createMockCreativePlayer();
		BlockPos center = relative.add(1, 0, 1);
		BlockPos centerAbs = context.getAbsolutePos(relative.add(1, 0, 1));
		entity.teleport(centerAbs.getX(), centerAbs.getY() + 2, centerAbs.getZ() + 1, false);
		entity.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, new Vec3d(centerAbs.getX(), centerAbs.getY(), centerAbs.getZ()));
		Set<BlockPos> selected = selector.select(center, entity);

		assertSelected(selected, square, context);
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "BlockSelectionTest")
	public void testVeinMiningDepth2(TestContext context) {
		BlockPos rootPos = context.getAbsolutePos(new BlockPos(0, 0, 0)).add(-3, 2, -3);
		BlockPos relative = context.getRelativePos(rootPos);

		Set<BlockPos> square = insert(createSquare(relative, 3, 3, 3), context);

		BlockSelector selector = selector(ModifiableFeatureAttribute.of(DEPTH_MODIFIER, 2), (entity, pos, root) -> context.getBlockState(pos).isOf(Blocks.STONE));
		Set<BlockPos> selected = selector.select(relative.add(1, 0, 1), context.createMockCreativePlayer());

		assertSelected(selected, square, context);
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "BlockSelectionTest")
	public void testVeinMiningDepth2FailsForTooBigSelection(TestContext context) {
		BlockPos rootPos = context.getAbsolutePos(new BlockPos(0, 0, 0)).add(-3, 2, -3);
		BlockPos relative = context.getRelativePos(rootPos);

		Set<BlockPos> square = insert(createSquare(relative, 4, 4, 4), context);

		BlockSelector selector = selector(ModifiableFeatureAttribute.of(DEPTH_MODIFIER, 2), (entity, pos, root) -> context.getBlockState(pos).isOf(Blocks.STONE));
		Set<BlockPos> selected = selector.select(relative.add(1, 0, 1), context.createMockCreativePlayer());

		try {
			assertSelected(selected, square, context);
			throw new GameTestException("Expected test to fail!");
		} catch (GameTestException e) {
			context.complete();
		}
	}

	private void assertSelected(Set<BlockPos> selected, Set<BlockPos> expected, TestContext context) {
		if (selected.size() != expected.size()) {
			throw new GameTestException("Expected " + expected.size() + " blocks to be selected, got " + selected.size());
		} else if (selected.stream().anyMatch(pos -> context.getBlockState(pos).isAir())) {
			throw new GameTestException("selected blocks should not be air");
		} else if (selected.containsAll(expected)) {
			context.complete();
		} else {
			throw new GameTestException("selected blocks did not match expected blocks");
		}
	}

	private Predicate<BlockPos> relativePredicate(BlockPos absolute, TestContext context) {
		return (BlockPos pos) -> isBreakableBlock(context.getWorld()).test(absolute.add(pos));
	}

	private BlockSelector selector(List<String> pattern) {
		return new PatternSelector(pattern, BlockFilter.DEFAULT, ModifiableFeatureAttribute.of(DEPTH_MODIFIER, 1), multiDirection);
	}

	private BlockSelector selector(ModifiableFeatureAttribute depth, BlockFilter filter) {
		return new RadiusVeinSelector(depth, filter);
	}

	public static Set<BlockPos> createSquare(BlockPos root, int height, int width, int depth) {
		Set<BlockPos> square = new HashSet<>();
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				for (int k = 0; k < depth; k++) {
					square.add(root.add(j, i, k));
				}
			}
		}
		return square;
	}

	public static Set<BlockPos> insert(Set<BlockPos> blocks, TestContext context, BlockState state) {
		for (BlockPos pos : blocks) {
			context.setBlockState(pos, state);
		}
		return blocks;
	}

	public static Set<BlockPos> insert(Set<BlockPos> blocks, TestContext context) {
		return insert(blocks, context, Blocks.STONE.getDefaultState());
	}

	public static List<String> pattern3x3() {
		List<String> pattern = new ArrayList<>();
		pattern.add("xxx");
		pattern.add("xxx");
		pattern.add("xxx");
		return pattern;
	}
}
