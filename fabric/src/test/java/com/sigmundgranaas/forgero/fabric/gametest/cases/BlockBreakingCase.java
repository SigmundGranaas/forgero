package com.sigmundgranaas.forgero.fabric.gametest.cases;

import static com.sigmundgranaas.forgero.fabric.gametest.helper.WorldBlockHelper.is;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.testutil.PlayerActionHelper;
import com.sigmundgranaas.forgero.testutil.TestPos;
import com.sigmundgranaas.forgero.testutil.TestPosCollection;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.test.GameTestException;

public class BlockBreakingCase {
	private final PlayerActionHelper helper;

	public BlockBreakingCase(PlayerActionHelper helper) {
		this.helper = helper;
	}

	public static BlockBreakingCase of(PlayerActionHelper helper) {
		return new BlockBreakingCase(helper);
	}

	public void assertCount(int expected, int actual, Supplier<String> message) {
		if (actual != expected) {
			throw new GameTestException(message.get());
		}
	}

	public void assertCount(int expected, int actual) {
		if (actual != expected) {
			throw new GameTestException("Expected " + expected + " blocks, but got: " + actual);
		}
	}

	public void assertCount(int expected, TestPosCollection collection, Predicate<TestPos> predicate) {
		long count = collection.count(predicate);
		if (count != expected) {
			String contents = collection.apply(predicate).toString();
			String message = String.format("Expected %s blocks, but got: %s. Here is the selection contents: %s", expected, count, contents);
			throw new GameTestException(message);
		}
	}

	private Predicate<TestPos> isBlock(Predicate<Block> predicate) {
		return pos -> predicate.test(helper.absolute(pos).getBlock());
	}

	private BlockState state(TestPos pos) {
		return helper.absolute(pos);
	}

	public Predicate<TestPos> stateToPos(Predicate<BlockState> statePredicate) {
		return pos -> statePredicate.test(state(pos));
	}

	public void assertBlockCount(int expected, TestPosCollection collection, Block block) {
		assertBlockCount(expected, collection, pos -> is(block).test(helper.absolute(pos).getBlock()));
	}

	public void assertBlockCount(int expected, TestPosCollection collection, Predicate<TestPos> predicate) {
		long count = collection.count(predicate);
		if (count != expected) {
			String contents = collection.apply(predicate).toString(helper);
			String message = String.format("Expected %s blocks, but got: %s. Here is the selection contents: %s", expected, count, contents);
			throw new GameTestException(message);
		}
	}

	public void assertBreakSelection(TestPosCollection selection, TestPos root, int ticks) {
		helper.processBreakingBlock(root, ticks);

		// No blocks should be left
		if (selection.anyMatch(notBroken())) {
			Forgero.LOGGER.warn("Not all blocks in the selection was broken, here is the result: {}", selection.apply(notBroken()).toString(helper));
			veinMiningErrors(root, ticks);
		}
	}

	public void assertNotBreakSelection(TestPosCollection selection, TestPos root, int ticks) {
		helper.processBreakingBlock(root, ticks);

		// No blocks should be broken
		if (selection.anyMatch(isBroken())) {
			throw new GameTestException("Blocks from the selection was broken, this means that blocks were mined in under the given ticks. Here is the result: " + selection.toString(helper));
		}
	}

	public void assertBreakSelection(TestPosCollection selection, TestPos root) {
		helper.processInstamine(root);

		// No blocks should be left
		if (selection.anyMatch(notBroken())) {
			Forgero.LOGGER.warn("Not all blocks in the selection was broken, here is the result: {}", selection.apply(notBroken()).toString(helper));
			veinMiningErrors(root, selection.apply(notBroken()));
		}
	}

	public void assertBreakBlock(TestPos root, int ticks) {
		assertBreakBlock(root, ticks, (state) -> String.format("Expected block to be air, got %s. Unable to mine block in under %s ticks", state.getBlock().toString(), ticks));
	}

	public void assertBreakBlock(TestPos root) {
		assertBreakBlock(root, (state) -> String.format("Expected block to be air, got %s. Unable to instamine", state.getBlock().toString()));
	}

	public void assertExists(TestPos root, Supplier<String> errorMessage) {
		if (isBroken(root)) {
			throw new GameTestException(errorMessage.get());
		}
	}

	public void assertExists(TestPos root, String errorMessage) {
		assertExists(root, () -> errorMessage);
	}

	public void assertBreakBlock(TestPos root, int ticks, Function<BlockState, String> message) {
		if (helper.get().getBlockState(root.relative()).isAir()) {
			throw new GameTestException("Block cannot be broken as it was already air, you probably chose the wrong block." + root.toString());
		}
		BlockState stoneResult = helper.processBreakingBlock(root, ticks);
		if (!stoneResult.isAir()) {
			throw new GameTestException(message.apply(stoneResult));
		}
	}

	public void assertBreakBlock(TestPos root, Function<BlockState, String> message) {
		BlockState stoneResult = helper.processInstamine(root);
		if (!stoneResult.isAir()) {
			throw new GameTestException(message.apply(stoneResult));
		}
	}

	public void assertNotBreakBlock(TestPos pos, int ticks) {
		assertNotBreakBlock(pos, ticks, () -> String.format("Expected block to be stay the same, got air, which means it mined the block in under %s ticks, which it should not do.", ticks));
	}

	public void assertNotBreakBlock(TestPos root, int ticks, String message) {
		assertNotBreakBlock(root, ticks, () -> message);
	}

	public void assertNotBreakBlock(TestPos root, int ticks, Supplier<String> message) {
		BlockState coalResult = helper.processBreakingBlock(root, ticks);

		if (coalResult.isAir()) {
			throw new GameTestException(message.get());
		}
	}

	private void veinMiningErrors(TestPos root, TestPosCollection remains) {
		if (isBroken().test(root)) {
			throw new GameTestException("Only mined center block, vein mining is not working as intended.");
		} else {
			throw new GameTestException(String.format("Expected to be able to instamine all blocks, but these were left: %s", remains.toString(helper)));
		}
	}

	private void veinMiningErrors(TestPos root, int ticks) {
		if (isBroken().test(root)) {
			throw new GameTestException("Only mined center block, vein mining is not working as intended.");
		} else {
			throw new GameTestException(String.format("Expected to be able to mine all blocks under %s ticks", ticks));
		}
	}

	private Predicate<TestPos> isBroken() {
		return this::isBroken;
	}

	private Predicate<TestPos> notBroken() {
		return isBroken().negate();
	}

	private boolean isBroken(TestPos pos) {
		return helper.absolute(pos).isAir();
	}
}
