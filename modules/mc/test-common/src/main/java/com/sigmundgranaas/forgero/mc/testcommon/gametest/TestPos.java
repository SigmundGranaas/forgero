package com.sigmundgranaas.forgero.mc.testcommon.gametest;

import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

/**
 * Represents a test position that tracks both absolute and relative coordinates.
 *
 * <p>GameTests work with two coordinate systems:
 * <ul>
 *   <li><b>Relative</b>: Position within the test structure (0,0,0 is the structure origin)
 *   <li><b>Absolute</b>: Position in the world (actual coordinates where test runs)
 * </ul>
 *
 * <p>This record makes it easy to work with both coordinate systems and apply offsets.
 *
 * <p>Example usage:
 * <pre>{@code
 * @GameTest
 * public void test_blocks(TestContext context) {
 *     // Create position at (1, 2, 3) relative to test structure
 *     TestPos pos = TestPos.of(new BlockPos(1, 2, 3), context);
 *
 *     // Get the absolute world position
 *     BlockPos worldPos = pos.absolute();
 *
 *     // Apply an offset to create a new position
 *     TestPos offsetPos = pos.offset(1, 0, 1);
 *
 *     context.setBlockState(offsetPos.relative(), Blocks.STONE.getDefaultState());
 *     context.complete();
 * }
 * }</pre>
 */
public record TestPos(BlockPos absoluteRoot, BlockPos relativeRoot, BlockPos offset) {

    /**
     * Gets the relative position (position within the test structure).
     *
     * @return the relative block position
     */
    public BlockPos relative() {
        return relativeRoot.add(offset);
    }

    /**
     * Gets the absolute position (position in the world).
     *
     * @return the absolute block position
     */
    public BlockPos absolute() {
        return absoluteRoot.add(offset);
    }

    /**
     * Creates a new TestPos with the specified offset applied.
     *
     * @param offset the offset to apply
     * @return a new TestPos with the offset
     */
    public TestPos offset(BlockPos offset) {
        return new TestPos(absolute(), relative(), offset);
    }

    /**
     * Creates a new TestPos with the specified offset applied.
     *
     * @param x the x offset
     * @param y the y offset
     * @param z the z offset
     * @return a new TestPos with the offset
     */
    public TestPos offset(int x, int y, int z) {
        return new TestPos(absolute(), relative(), new BlockPos(x, y, z));
    }

    /**
     * Creates a TestPos from a relative position and test context.
     *
     * @param relative the relative position within the test structure
     * @param context the test context
     * @return a new TestPos
     */
    public static TestPos of(BlockPos relative, TestContext context) {
        return new TestPos(context.getAbsolutePos(relative), relative, BlockPos.ORIGIN);
    }

    /**
     * Creates a TestPos from absolute and relative positions.
     *
     * @param absolute the absolute world position
     * @param relative the relative structure position
     * @return a new TestPos
     */
    public static TestPos of(BlockPos absolute, BlockPos relative) {
        return new TestPos(absolute, relative, BlockPos.ORIGIN);
    }

    /**
     * Creates a TestPos from an existing TestPos with a new offset.
     *
     * @param pos the existing TestPos
     * @param offset the new offset to apply
     * @return a new TestPos with the offset
     */
    public static TestPos of(TestPos pos, BlockPos offset) {
        return new TestPos(pos.absoluteRoot, pos.relativeRoot, offset);
    }
}
