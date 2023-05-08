package com.sigmundgranaas.forgerofabric.gametest;

import static com.sigmundgranaas.forgero.minecraft.common.toolhandler.block.BlockUtils.isBreakableBlock;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import com.sigmundgranaas.forgero.minecraft.common.toolhandler.block.selector.BlockSelector;
import com.sigmundgranaas.forgero.minecraft.common.toolhandler.block.selector.PatternSelector;
import com.sigmundgranaas.forgero.minecraft.common.toolhandler.block.selector.RadiusVeinSelector;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;

@SuppressWarnings("unused")
public class BlockSelectionTest {

	@GameTest(structureName = FabricGameTest.EMPTY_STRUCTURE, batchId = "BlockSelectionTest")
	public void test3x3PatternSelectionNorth(TestContext context) {
		BlockPos rootPos = context.getAbsolutePos(new BlockPos(0, 0, 0)).add(-3, 1, -3);
		BlockPos relative = context.getRelativePos(rootPos);

		Set<BlockPos> square = insert(createSquare(relative, 3, 3, 1), context);

		BlockPos centerOffset = new BlockPos(1, 1, 0);
		BlockSelector selector = selector(pattern3x3(), Direction.NORTH, Direction.EAST);
		Set<BlockPos> selected = selector.select(relative.add(centerOffset));

		assertSelected(selected, square, context);
	}

	@GameTest(structureName = FabricGameTest.EMPTY_STRUCTURE, batchId = "BlockSelectionTest")
	public void test3x3PatternSelectionEast(TestContext context) {
		BlockPos rootPos = context.getAbsolutePos(new BlockPos(0, 0, 0)).add(-3, 1, -3);
		BlockPos relative = context.getRelativePos(rootPos);

		Set<BlockPos> square = insert(createSquare(relative, 3, 1, 3), context);

		BlockSelector selector = selector(pattern3x3(), Direction.EAST, Direction.NORTH);
		Set<BlockPos> selected = selector.select(relative.add(0, 1, 1));

		assertSelected(selected, square, context);
	}

	@GameTest(structureName = FabricGameTest.EMPTY_STRUCTURE, batchId = "BlockSelectionTest")
	public void test3x3PatternSelectionDown(TestContext context) {
		BlockPos rootPos = context.getAbsolutePos(new BlockPos(0, 0, 0)).add(-3, 1, -3);
		BlockPos relative = context.getRelativePos(rootPos);

		Set<BlockPos> square = insert(createSquare(relative, 1, 3, 3), context);

		BlockSelector selector = selector(pattern3x3(), Direction.DOWN, Direction.EAST);
		Set<BlockPos> selected = selector.select(relative.add(1, 0, 1));

		assertSelected(selected, square, context);
	}

	@GameTest(structureName = FabricGameTest.EMPTY_STRUCTURE, batchId = "BlockSelectionTest")
	public void testVeinMiningDepth2(TestContext context) {
		BlockPos rootPos = context.getAbsolutePos(new BlockPos(0, 0, 0)).add(-3, 2, -3);
		BlockPos relative = context.getRelativePos(rootPos);

		Set<BlockPos> square = insert(createSquare(relative, 3, 3, 3), context);

		BlockSelector selector = selector(2, (pos) -> context.getBlockState(pos).isOf(Blocks.STONE));
		Set<BlockPos> selected = selector.select(relative.add(1, 0, 1));

		assertSelected(selected, square, context);
	}

	@GameTest(structureName = FabricGameTest.EMPTY_STRUCTURE, batchId = "BlockSelectionTest")
	public void testVeinMiningDepth2FailsForTooBigSelection(TestContext context) {
		BlockPos rootPos = context.getAbsolutePos(new BlockPos(0, 0, 0)).add(-3, 2, -3);
		BlockPos relative = context.getRelativePos(rootPos);

		Set<BlockPos> square = insert(createSquare(relative, 4, 4, 4), context);

		BlockSelector selector = selector(2, (pos) -> context.getBlockState(pos).isOf(Blocks.STONE));
		Set<BlockPos> selected = selector.select(relative.add(1, 0, 1));

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

	private Set<BlockPos> createSquare(BlockPos root, int height, int width, int depth) {
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


	private Set<BlockPos> insert(Set<BlockPos> blocks, TestContext context, BlockState state) {
		for (BlockPos pos : blocks) {
			context.setBlockState(pos, state);
		}
		return blocks;
	}

	private Set<BlockPos> insert(Set<BlockPos> blocks, TestContext context) {
		return insert(blocks, context, Blocks.STONE.getDefaultState());
	}

	private List<String> pattern3x3() {
		List<String> pattern = new ArrayList<>();
		pattern.add("xxx");
		pattern.add("xxx");
		pattern.add("xxx");
		return pattern;
	}

	private Predicate<BlockPos> relativePredicate(BlockPos absolute, TestContext context) {
		return (BlockPos pos) -> isBreakableBlock(context.getWorld()).test(absolute.add(pos));
	}

	private BlockSelector selector(List<String> pattern, Direction primary, Direction secondary) {
		return new PatternSelector(pattern, new Direction[]{primary, secondary}, primary);
	}

	private BlockSelector selector(int depth, Predicate<BlockPos> predicate) {
		return new RadiusVeinSelector(depth, predicate);
	}

}
