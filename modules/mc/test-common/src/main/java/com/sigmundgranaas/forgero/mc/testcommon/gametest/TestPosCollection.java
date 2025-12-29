package com.sigmundgranaas.forgero.mc.testcommon.gametest;

import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A collection of TestPos instances with utility methods for batch operations.
 *
 * <p>This record provides a convenient way to work with multiple test positions at once,
 * offering filtering, mapping, and transformation operations.
 *
 * <p>Example usage:
 * <pre>{@code
 * @GameTest
 * public void test_multiple_positions(TestContext context) {
 *     // Create a collection of positions
 *     Set<BlockPos> relativePositions = Set.of(
 *         new BlockPos(1, 1, 1),
 *         new BlockPos(2, 1, 2),
 *         new BlockPos(3, 1, 3)
 *     );
 *     TestPosCollection positions = TestPosCollection.of(relativePositions, context);
 *
 *     // Get all absolute positions
 *     Set<BlockPos> absolutePositions = positions.absolute();
 *
 *     // Filter positions based on a condition
 *     TestPosCollection filtered = positions.apply(
 *         pos -> pos.relative().getX() > 1
 *     );
 *
 *     // Count positions matching a predicate
 *     long count = positions.count(pos -> pos.relative().getY() == 1);
 *
 *     context.assertTrue(count == 3, "Should have 3 positions at Y=1");
 *     context.complete();
 * }
 * }</pre>
 */
public record TestPosCollection(Set<TestPos> positions) {

    /**
     * Creates a TestPosCollection from a set of relative positions.
     *
     * @param relative the relative positions within the test structure
     * @param ctx the test context
     * @return a new TestPosCollection
     */
    public static TestPosCollection of(Set<BlockPos> relative, TestContext ctx) {
        var positions = relative.stream()
                .map(pos -> TestPos.of(pos, ctx))
                .collect(Collectors.toSet());
        return new TestPosCollection(positions);
    }

    /**
     * Creates a TestPosCollection from a set of absolute positions.
     *
     * @param absolute the absolute world positions
     * @param ctx the test context
     * @return a new TestPosCollection
     */
    public static TestPosCollection ofAbsolute(Set<BlockPos> absolute, TestContext ctx) {
        var positions = absolute.stream()
                .map(pos -> TestPos.of(pos, ctx.getRelativePos(pos)))
                .collect(Collectors.toSet());
        return new TestPosCollection(positions);
    }

    /**
     * Creates a TestPosCollection from a set of TestPos instances.
     *
     * @param positions the test positions
     * @return a new TestPosCollection
     */
    public static TestPosCollection of(Set<TestPos> positions) {
        return new TestPosCollection(positions);
    }

    /**
     * Gets all relative positions in this collection.
     *
     * @return a set of relative block positions
     */
    public Set<BlockPos> relative() {
        return positions.stream()
                .map(TestPos::relative)
                .collect(Collectors.toSet());
    }

    /**
     * Gets all absolute positions in this collection.
     *
     * @return a set of absolute block positions
     */
    public Set<BlockPos> absolute() {
        return positions.stream()
                .map(TestPos::absolute)
                .collect(Collectors.toSet());
    }

    /**
     * Checks if any position in the collection matches the given predicate.
     *
     * @param predicate the condition to test
     * @return true if any position matches
     */
    public boolean anyMatch(Predicate<TestPos> predicate) {
        return positions.stream().anyMatch(predicate);
    }

    /**
     * Filters the collection to only include positions matching the predicate.
     *
     * @param predicate the filter condition
     * @return a new TestPosCollection with only matching positions
     */
    public TestPosCollection apply(Predicate<TestPos> predicate) {
        return of(positions.stream()
                .filter(predicate)
                .collect(Collectors.toSet()));
    }

    /**
     * Counts how many positions match the given predicate.
     *
     * @param predicate the condition to test
     * @return the number of matching positions
     */
    public long count(Predicate<TestPos> predicate) {
        return positions.stream()
                .filter(predicate)
                .count();
    }

    @Override
    public String toString() {
        return "TestPosCollection{" +
                "positions=" + positions.toString() +
                '}';
    }

    /**
     * Returns a detailed string representation including block information.
     *
     * @param ctx the context supplier for accessing block states
     * @return a detailed string with block IDs and positions
     */
    public String toString(ContextSupplier ctx) {
        String elements = positions.stream()
                .map(pos -> {
                    BlockState state = ctx.absolute(pos);
                    String blockId = Registries.BLOCK.getId(state.getBlock()).toString();
                    return String.format("{ Block: %s, pos: %s }", blockId, pos);
                })
                .collect(Collectors.joining(", "));

        return "TestPosCollection{" +
                "elements=" + elements +
                '}';
    }
}
