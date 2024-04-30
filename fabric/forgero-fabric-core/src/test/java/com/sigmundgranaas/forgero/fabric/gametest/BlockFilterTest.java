package com.sigmundgranaas.forgero.fabric.gametest;

import java.util.List;

import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.filter.BlockFilter;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.filter.CanMineFilter;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.filter.IsBlockFilter;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.filter.SameBlockFilter;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.filter.SimilarBlockFilter;
import com.sigmundgranaas.forgero.minecraft.common.match.predicate.BlockPredicateMatcher;
import com.sigmundgranaas.forgero.testutil.PlayerFactory;
import com.sigmundgranaas.forgero.testutil.TestPos;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;

@SuppressWarnings("unused")
public class BlockFilterTest {
	public static BlockPos POS = new BlockPos(1, 1, 1);

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "BlockFilterTest")
	public void testBlockFilter(TestContext context) {
		// Prepare filters
		List<BlockFilter> filters = List.of(isBlock(), isBlockObject(), isBlockObject());
		TestPos pos = TestPos.of(POS, context);

		filters.forEach(filter -> {
			context.setBlockState(pos.relative(), Blocks.AIR);
			assertFalse(pos, filter, context);

			context.setBlockState(pos.relative(), Blocks.STONE);
			assertTrue(pos, filter, context);
		});

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "BlockFilterTest")
	public void testSameBlockFilter(TestContext context) {
		// Prepare filters
		BlockFilter filter = sameBlockFilter();
		TestPos root = TestPos.of(POS, context);
		TestPos current = root.offset(new BlockPos(0, 1, 0));

		context.setBlockState(root.relative(), Blocks.AIR);
		context.setBlockState(current.relative(), Blocks.AIR);

		assertTrue(root, current, filter, context);

		context.setBlockState(current.relative(), Blocks.STONE);
		assertFalse(root, current, filter, context);

		context.setBlockState(root.relative(), Blocks.STONE);
		assertTrue(root, current, filter, context);

		context.complete();
	}

	/**
	 * Ensures that the similar block filer works as expected. Similar blocks have to be defined using the custom tag location:
	 * <pre>tags/blocks/similar_block/tag_name.json</pre>
	 * Here is example content:
	 * <pre>
	 * {
	 *     "replace": false,
	 *     "values": [
	 *          "minecraft:oak_wood",
	 *         "minecraft:oak_planks"
	 *     ]
	 * }
	 * </pre>
	 *
	 * @param context test context
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "BlockFilterTest")
	public void testSimilarBlockFilter(TestContext context) {
		// Prepare filters
		BlockFilter filter = similarBlockFilter();
		TestPos root = TestPos.of(POS, context);
		TestPos current = root.offset(new BlockPos(0, 1, 0));

		context.setBlockState(root.relative(), Blocks.AIR);
		context.setBlockState(current.relative(), Blocks.AIR);

		assertTrue(root, current, filter, context);

		context.setBlockState(current.relative(), Blocks.OAK_WOOD);
		assertFalse(root, current, filter, context);

		context.setBlockState(root.relative(), Blocks.OAK_PLANKS);
		assertTrue(root, current, filter, context);

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "BlockFilterTest")
	public void testCanMineFilter(TestContext context) {
		// Prepare filters
		BlockFilter filter = canMine();
		TestPos pos = TestPos.of(POS, context);
		PlayerEntity player = PlayerFactory.builder(context)
				.gameMode(GameMode.SURVIVAL)
				.pos(pos.absolute())
				.build()
				.createPlayer();

		context.setBlockState(pos.relative(), Blocks.AIR);
		assertFalse(pos, pos, filter, context, player);

		context.setBlockState(pos.relative(), Blocks.STONE);
		assertFalse(pos, pos, filter, context, player);

		player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.DIAMOND_PICKAXE));
		assertTrue(pos, pos, filter, context, player);

		player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Registries.ITEM.get(new Identifier("forgero:diamond-pickaxe"))));
		assertTrue(pos, pos, filter, context, player);

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "BlockFilterTest")
	public void testBlockTagFilter(TestContext context) {
		// Prepare filters
		BlockFilter filter = blockTag();
		TestPos pos = TestPos.of(POS, context);

		context.setBlockState(pos.relative(), Blocks.AIR);
		assertFalse(pos, filter, context);

		context.setBlockState(pos.relative(), Blocks.STONE);
		assertFalse(pos, filter, context);

		context.setBlockState(pos.relative(), Blocks.COAL_ORE);
		assertTrue(pos, filter, context);

		context.complete();
	}


	public static void assertTrue(TestPos root, TestPos pos, BlockFilter filter, TestContext context) {
		assertTrue(root, pos, filter, context, context.createMockCreativePlayer());

	}

	public static void assertFalse(TestPos root, TestPos pos, BlockFilter filter, TestContext context) {
		assertFalse(root, pos, filter, context, context.createMockCreativePlayer());
	}

	public static void assertTrue(TestPos pos, BlockFilter filter, TestContext context) {
		assertTrue(pos, pos, filter, context, context.createMockCreativePlayer());

	}

	public static void assertFalse(TestPos pos, BlockFilter filter, TestContext context) {
		assertFalse(pos, pos, filter, context, context.createMockCreativePlayer());
	}

	public static void assertTrue(TestPos root, TestPos current, BlockFilter filter, TestContext context, PlayerEntity entity) {
		boolean result = filter.filter(entity, current.absolute(), root.absolute());
		if (!result) {
			throw new GameTestException("Expected filtered block to return true, but it did not. Block: \n" + context.getBlockState(current.relative()).toString());
		}
	}

	public static void assertFalse(TestPos root, TestPos current, BlockFilter filter, TestContext context, PlayerEntity entity) {
		boolean result = filter.filter(entity, current.absolute(), root.absolute());
		if (result) {
			throw new GameTestException("Expected filtered block to return false, but it did not. Current Block: " + context.getBlockState(current.relative()).toString());
		}
	}

	public static BlockFilter isBlockObject() {
		String filter = """
				{
				"type": "forgero:is_block"
				}
				""";
		return IsBlockFilter.BUILDER.build(JsonParser.parseString(filter)).orElseThrow();
	}

	public static BlockFilter isBlockArray() {
		String filter = """
				[
					"forgero:is_block",
					{
					"type": "forgero:is_block"
					}
				 ]
				""";
		return IsBlockFilter.BUILDER.build(JsonParser.parseString(filter)).orElseThrow();
	}

	public static BlockFilter isBlock() {
		String filter = "forgero:is_block";
		return IsBlockFilter.BUILDER.build(new JsonPrimitive(filter)).orElseThrow();
	}

	public static BlockFilter canMine() {
		String filter = "forgero:can_mine";
		return CanMineFilter.BUILDER.build(new JsonPrimitive(filter)).orElseThrow();
	}

	public static BlockFilter sameBlockFilter() {
		String filter = "forgero:same_block";
		return SameBlockFilter.BUILDER.build(new JsonPrimitive(filter)).orElseThrow();
	}

	public static BlockFilter similarBlockFilter() {
		String filter = "forgero:similar_block";
		return SimilarBlockFilter.BUILDER.build(new JsonPrimitive(filter)).orElseThrow();
	}

	public static BlockFilter blockTag() {
		String filter = """
				{
					"type": "minecraft:block",
				    "tag": "forgero:vein_mining_ores"
				}
				""";
		return BlockPredicateMatcher.BUILDER.build(JsonParser.parseString(filter)).orElseThrow();
	}
}
