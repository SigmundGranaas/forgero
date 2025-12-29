package com.sigmundgranaas.forgero.mc.testcommon.gametest;

import net.minecraft.block.BlockState;
import net.minecraft.test.TestContext;

import java.util.function.Supplier;

/**
 * A functional interface that supplies a TestContext and provides convenience methods
 * for accessing block states at test positions.
 *
 * <p>This interface extends Supplier&lt;TestContext&gt; and adds helper methods for working
 * with TestPos instances to get block states in both relative and absolute coordinates.
 *
 * <p>Example usage:
 * <pre>{@code
 * @GameTest
 * public void test_blocks(TestContext context) {
 *     ContextSupplier ctx = ContextSupplier.of(context);
 *     TestPos pos = TestPos.of(new BlockPos(1, 2, 3), context);
 *
 *     // Get block state at relative position
 *     BlockState relativeBlock = ctx.relative(pos);
 *
 *     // Get block state at absolute position
 *     BlockState absoluteBlock = ctx.absolute(pos);
 *
 *     context.assertTrue(
 *         relativeBlock.isOf(Blocks.STONE),
 *         "Block should be stone"
 *     );
 *     context.complete();
 * }
 * }</pre>
 */
public interface ContextSupplier extends Supplier<TestContext> {

    /**
     * Creates a ContextSupplier from a TestContext.
     *
     * @param ctx the test context
     * @return a ContextSupplier that provides the given context
     */
    static ContextSupplier of(TestContext ctx) {
        return () -> ctx;
    }

    /**
     * Gets the block state at the relative position.
     *
     * @param pos the test position
     * @return the block state at the relative position
     */
    default BlockState relative(TestPos pos) {
        return get().getBlockState(pos.relative());
    }

    /**
     * Gets the block state at the absolute position.
     *
     * @param pos the test position
     * @return the block state at the absolute position
     */
    default BlockState absolute(TestPos pos) {
        return get().getWorld().getBlockState(pos.absolute());
    }
}
