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
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.selector.ColumnSelector;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.selector.PatternSelector;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.selector.RadiusVeinSelector;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.selector.SingleSelector;
import com.sigmundgranaas.forgero.testutil.TestPos;
import com.sigmundgranaas.forgero.testutil.TestPosCollection;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;

@SuppressWarnings("unused")
public class BlockSelectionTest {
	public static BlockPos RELATIVE_3X3_CENTER = new BlockPos(1, 1, 1);
	public static BlockPos RELATIVE_5X5_CENTER = new BlockPos(2, 2, 2);
	public static BlockPos RELATIVE_7X7_CENTER = new BlockPos(3, 3, 3);

	public static BlockPos RELATIVE_3X3_NORTH = new BlockPos(1, 2, 0);
	public static BlockPos RELATIVE_3X3_EAST = new BlockPos(0, 2, 2);
	public static BlockPos RELATIVE_3X3_DOWN = new BlockPos(1, 1, 1);


	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "BlockSelectionTest")
	public void test3x3PatternSelectionNorth(TestContext context) {
		// Prepare selector
		BlockSelector selector = selector3x3();

		TestPos center = TestPos.of(RELATIVE_3X3_NORTH, context);
		TestPosCollection square = TestPosCollection.of(insert(createSquare(TestPos.of(BlockPos.ORIGIN.up(), context), 3, 3, 1), context));

		Set<BlockPos> selected = selector.select(center.absolute(), context.createMockCreativePlayer());

		assertSelected(selected, square, context);
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "BlockSelectionTest")
	public void singleSelectorTest(TestContext context) {
		// Prepare selector
		BlockSelector selector = selectorSingle();

		TestPos center = TestPos.of(RELATIVE_3X3_NORTH, context);
		TestPosCollection square = TestPosCollection.of(insert(createSquare(TestPos.of(new BlockPos(0, 1, 0), context), 3, 3, 1), context));

		Set<BlockPos> selected = selector.select(center.absolute(), context.createMockCreativePlayer());

		if (selected.size() == 1) {
			context.complete();
		} else {
			throw new GameTestException("Did not only select a single block. \n Here is the selection: " + TestPosCollection.ofAbsolute(selected, context).toString(() -> context));
		}
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "BlockSelectionTest")
	public void test3x3CrossPattern(TestContext context) {
		// Prepare selector
		BlockSelector selector = selectorCross();

		TestPos center = TestPos.of(RELATIVE_3X3_NORTH, context);
		TestPosCollection square = TestPosCollection.of(insert(createSquare(TestPos.of(new BlockPos(0, 1, 0), context), 3, 3, 1), context));

		Set<BlockPos> selected = selector.select(center.absolute(), context.createMockCreativePlayer());

		if (selected.size() == 5) {
			context.complete();
		} else {
			throw new GameTestException("Did not select the proper blocks in a cross pattern. \n Here is the selection: " + TestPosCollection.ofAbsolute(selected, context).toString(() -> context));
		}
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "BlockSelectionTest")
	public void test3x3PatternSelectionEast(TestContext context) {
		// Prepare selector
		BlockSelector selector = selector3x3();

		TestPos center = TestPos.of(RELATIVE_3X3_EAST, context);
		TestPosCollection square = TestPosCollection.of(insert(createSquare(TestPos.of(BlockPos.ORIGIN.add(0, 1, 1), context), 3, 1, 3), context));

		PlayerEntity entity = context.createMockCreativePlayer();
		entity.teleport(center.absolute().getX(), center.absolute().getY(), center.absolute().getZ() + 1, false);
		entity.setHeadYaw(Direction.EAST.asRotation());
		entity.setYaw(Direction.EAST.asRotation());
		entity.baseTick();

		Set<BlockPos> selected = selector.select(center.absolute(), entity);

		assertSelected(selected, square, context);
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "BlockSelectionTest")
	public void test3x3PatternSelectionDown(TestContext context) {
		// Prepare selector
		BlockSelector selector = selector3x3();

		TestPos center = TestPos.of(RELATIVE_3X3_DOWN, context);
		TestPosCollection square = TestPosCollection.of(insert(createSquare(TestPos.of(BlockPos.ORIGIN.up(), context), 1, 3, 3), context));

		PlayerEntity entity = context.createMockCreativePlayer();
		BlockPos centerAbs = center.absolute();
		entity.teleport(centerAbs.getX(), centerAbs.getY() + 2, centerAbs.getZ() + 1, false);
		entity.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, new Vec3d(centerAbs.getX(), centerAbs.getY(), centerAbs.getZ()));


		Set<BlockPos> selected = selector.select(center.absolute(), entity);

		assertSelected(selected, square, context);
	}


	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "BlockSelectionTest")
	public void testVeinMiningDepth2(TestContext context) {
		// Prepare selector
		BlockSelector selector = radius2();

		// Block setup
		TestPos center = TestPos.of(RELATIVE_5X5_CENTER, context);
		TestPosCollection square = TestPosCollection.of(insert(createSquare(center, 3, 3, 3), context));

		Set<BlockPos> selected = selector.select(center.absolute(), context.createMockCreativePlayer());

		assertSelected(selected, square, context);
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "BlockSelectionTest")
	public void testVeinMiningDepth2FailsForTooBigSelection(TestContext context) {
		// Prepare selector
		BlockSelector selector = radius2();
		TestPos center = TestPos.of(RELATIVE_7X7_CENTER, context);

		TestPosCollection square = TestPosCollection.of(insert(createSquare(center, 7, 7, 7), context));

		Set<BlockPos> selected = selector.select(center.absolute(), context.createMockCreativePlayer());

		if (selected.size() < square.relative().size()) {
			try {
				assertSelected(selected, square, context);
				throw new GameTestException("Expected test to fail!");
			} catch (GameTestException e) {
				context.complete();
			}
		} else {
			throw new GameTestException("Selection is bigger than the square, the selection algorithm is not functioning correctly.");
		}
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "BlockSelectionTest")
	public void testColumnMiningSingleColumn(TestContext context) {
		// Prepare selector
		BlockSelector selector = columnX1();
		TestPos center = TestPos.of(BlockPos.ORIGIN.up(), context);

		TestPosCollection square = TestPosCollection.of(insert(createSquare(center, 5, 1, 1), context));
		Set<BlockPos> selected = selector.select(center.absolute(), context.createMockCreativePlayer());

		assertSelected(selected, square, context);
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "BlockSelectionTest")
	public void testColumnMiningMultipleColumns(TestContext context) {
		// Prepare selector
		BlockSelector selector = columnX4();
		TestPos center = TestPos.of(BlockPos.ORIGIN.up(), context);

		TestPosCollection square = TestPosCollection.of(insert(createSquare(center, 5, 2, 2), context));
		Set<BlockPos> selected = selector.select(center.absolute(), context.createMockCreativePlayer());

		assertSelected(selected, square, context);
	}


	private void assertSelected(Set<BlockPos> selected, TestPosCollection expected, TestContext context) {
		if (selected.size() != expected.relative().size()) {
			throw new GameTestException("Expected " + expected.relative().size() + " blocks to be selected, got " + selected.size());
		} else if (selected.stream().anyMatch(pos -> context.getWorld().getBlockState(pos).isAir())) {
			throw new GameTestException("selected blocks should not be air");
		} else if (selected.containsAll(expected.absolute())) {
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

	public static Set<TestPos> createSquare(TestPos root, int height, int width, int depth) {
		Set<TestPos> square = new HashSet<>();
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				for (int k = 0; k < depth; k++) {
					square.add(root.offset(new BlockPos(j, i, k)));
				}
			}
		}
		return square;
	}

	public static Set<TestPos> createPlane(TestPos root, int width, int depth) {
		Set<TestPos> square = new HashSet<>();
		for (int j = 0; j < width; j++) {
			for (int k = 0; k < depth; k++) {
				square.add(root.offset(new BlockPos(j, root.relative().getY(), k)));
			}
		}
		return square;
	}

	public static Set<TestPos> insert(Set<TestPos> blocks, TestContext context, BlockState state) {
		for (TestPos pos : blocks) {
			context.setBlockState(pos.relative(), state);
		}
		return blocks;
	}

	public static Set<TestPos> insert(Set<TestPos> blocks, TestContext context) {
		return insert(blocks, context, Blocks.STONE.getDefaultState());
	}

	public static List<String> pattern3x3() {
		List<String> pattern = new ArrayList<>();
		pattern.add("xxx");
		pattern.add("xxx");
		pattern.add("xxx");
		return pattern;
	}

	public static BlockSelector selector3x3() {
		String selectorJson = """
				{
					"type": "forgero:pattern",
					"pattern": [
					"xxx",
					"xxx",
					"xxx"
					],
					"filter": "forgero:is_block"
				}
					
				""";

		return Utils.handlerFromString(selectorJson, PatternSelector.BUILDER);
	}

	public static BlockSelector columnX1() {
		String selectorJson = """
				{
					"type": "forgero:column",
					"depth": 1,
					"height": 5,
					"filter": "forgero:is_block"
				}
					
				""";

		return Utils.handlerFromString(selectorJson, ColumnSelector.BUILDER);
	}

	public static BlockSelector columnX4() {
		String selectorJson = """
				{
					"type": "forgero:column",
					"depth": 4,
					"height": 5,
					"filter": "forgero:is_block"
				}
					
				""";

		return Utils.handlerFromString(selectorJson, ColumnSelector.BUILDER);
	}

	public static BlockSelector selectorCross() {
		String selectorJson = """
				{
					"type": "forgero:pattern",
					"pattern": [
					"x x",
					" x ",
					"x x"
					],
					"filter": "forgero:is_block"
				}
					
				""";

		return Utils.handlerFromString(selectorJson, PatternSelector.BUILDER);
	}

	public static BlockSelector selector2x1() {
		String selectorJson = """
				{
					"type": "forgero:pattern",
					"pattern": [
					"X",
					"x"
					],
					"filter": "forgero:is_block"

				}
					
				""";

		return Utils.handlerFromString(selectorJson, PatternSelector.BUILDER);
	}


	public static BlockSelector radius2() {
		String selectorJson = """
				{
					"type": "forgero:radius",
					"radius": 2,
					"filter": "forgero:is_block"
				}
					
				""";

		return Utils.handlerFromString(selectorJson, RadiusVeinSelector.BUILDER);
	}

	public static BlockSelector selectorSingle() {
		String selectorJson = """
				{
					"type": "forgero:single",
					"filter": "forgero:is_block"
				}
					
				""";

		return Utils.handlerFromString(selectorJson, SingleSelector.BUILDER);
	}
}
