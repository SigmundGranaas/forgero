package com.sigmundgranaas.forgero.mc.testcommon;

import com.sigmundgranaas.forgero.mc.testcommon.gametest.ContextSupplier;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.TestPos;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.TestPosCollection;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

import java.util.Set;

/**
 * GameTests for position utilities (TestPos, TestPosCollection, ContextSupplier).
 */
public class PositionUtilityTests implements ForgeroGameTest {

    // ========== TestPos Tests ==========

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testPos_of_shouldCreateFromRelative(TestContext context) {
        BlockPos relative = new BlockPos(1, 2, 3);
        TestPos pos = TestPos.of(relative, context);

        context.assertTrue(pos != null, "TestPos should be created");
        context.assertTrue(pos.relative().equals(relative), "Relative position should match");
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testPos_absolute_shouldMatchContext(TestContext context) {
        BlockPos relative = new BlockPos(1, 2, 3);
        TestPos pos = TestPos.of(relative, context);

        BlockPos expectedAbsolute = context.getAbsolutePos(relative);
        context.assertTrue(pos.absolute().equals(expectedAbsolute),
                "Absolute position should match context");
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testPos_offset_shouldApplyOffset(TestContext context) {
        TestPos pos = TestPos.of(new BlockPos(1, 1, 1), context);
        TestPos offset = pos.offset(2, 0, 2);

        BlockPos expectedRelative = new BlockPos(3, 1, 3);
        context.assertTrue(offset.relative().equals(expectedRelative),
                "Offset should be applied to relative position");
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testPos_offsetBlockPos_shouldApplyOffset(TestContext context) {
        TestPos pos = TestPos.of(new BlockPos(0, 0, 0), context);
        TestPos offset = pos.offset(new BlockPos(5, 10, 15));

        context.assertTrue(offset.relative().getX() == 5, "X offset should be 5");
        context.assertTrue(offset.relative().getY() == 10, "Y offset should be 10");
        context.assertTrue(offset.relative().getZ() == 15, "Z offset should be 15");
        context.complete();
    }

    // ========== TestPosCollection Tests ==========

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testPosCollection_of_shouldCreateFromRelative(TestContext context) {
        Set<BlockPos> relative = Set.of(
                new BlockPos(1, 1, 1),
                new BlockPos(2, 2, 2),
                new BlockPos(3, 3, 3)
        );
        TestPosCollection collection = TestPosCollection.of(relative, context);

        context.assertTrue(collection.positions().size() == 3,
                "Collection should have 3 positions");
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testPosCollection_relative_shouldReturnRelativePositions(TestContext context) {
        Set<BlockPos> expected = Set.of(
                new BlockPos(1, 1, 1),
                new BlockPos(2, 2, 2)
        );
        TestPosCollection collection = TestPosCollection.of(expected, context);

        Set<BlockPos> actual = collection.relative();
        context.assertTrue(actual.equals(expected),
                "Relative positions should match original");
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testPosCollection_absolute_shouldReturnAbsolutePositions(TestContext context) {
        Set<BlockPos> relative = Set.of(new BlockPos(1, 1, 1));
        TestPosCollection collection = TestPosCollection.of(relative, context);

        Set<BlockPos> absolute = collection.absolute();
        BlockPos expected = context.getAbsolutePos(new BlockPos(1, 1, 1));

        context.assertTrue(absolute.contains(expected),
                "Absolute positions should be converted correctly");
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testPosCollection_anyMatch_shouldDetectMatches(TestContext context) {
        Set<BlockPos> relative = Set.of(
                new BlockPos(1, 1, 1),
                new BlockPos(5, 5, 5)
        );
        TestPosCollection collection = TestPosCollection.of(relative, context);

        boolean hasHighX = collection.anyMatch(pos -> pos.relative().getX() > 4);
        context.assertTrue(hasHighX, "Should find position with X > 4");

        boolean hasNegative = collection.anyMatch(pos -> pos.relative().getX() < 0);
        context.assertFalse(hasNegative, "Should not find negative X");

        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testPosCollection_apply_shouldFilter(TestContext context) {
        Set<BlockPos> relative = Set.of(
                new BlockPos(1, 1, 1),
                new BlockPos(2, 2, 2),
                new BlockPos(3, 3, 3),
                new BlockPos(4, 4, 4)
        );
        TestPosCollection collection = TestPosCollection.of(relative, context);

        TestPosCollection filtered = collection.apply(pos -> pos.relative().getX() > 2);

        context.assertTrue(filtered.positions().size() == 2,
                "Should have 2 positions with X > 2");
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testPosCollection_count_shouldCountMatches(TestContext context) {
        Set<BlockPos> relative = Set.of(
                new BlockPos(1, 5, 1),
                new BlockPos(2, 5, 2),
                new BlockPos(3, 10, 3)
        );
        TestPosCollection collection = TestPosCollection.of(relative, context);

        long count = collection.count(pos -> pos.relative().getY() == 5);
        context.assertTrue(count == 2, "Should count 2 positions with Y=5");

        context.complete();
    }

    // ========== ContextSupplier Tests ==========

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void contextSupplier_of_shouldWrapContext(TestContext context) {
        ContextSupplier supplier = ContextSupplier.of(context);

        context.assertTrue(supplier != null, "ContextSupplier should be created");
        context.assertTrue(supplier.get() == context, "Should return the wrapped context");
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void contextSupplier_relative_shouldGetBlockState(TestContext context) {
        // Set a block at position (1, 1, 1)
        BlockPos pos = new BlockPos(1, 1, 1);
        context.setBlockState(pos, Blocks.STONE);

        ContextSupplier supplier = ContextSupplier.of(context);
        TestPos testPos = TestPos.of(pos, context);

        BlockState state = supplier.relative(testPos);
        context.assertTrue(state.isOf(Blocks.STONE),
                "Should get stone block at relative position");
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void contextSupplier_absolute_shouldGetBlockState(TestContext context) {
        // Set a block at position (2, 2, 2)
        BlockPos pos = new BlockPos(2, 2, 2);
        context.setBlockState(pos, Blocks.DIAMOND_BLOCK);

        ContextSupplier supplier = ContextSupplier.of(context);
        TestPos testPos = TestPos.of(pos, context);

        BlockState state = supplier.absolute(testPos);
        context.assertTrue(state.isOf(Blocks.DIAMOND_BLOCK),
                "Should get diamond block at absolute position");
        context.complete();
    }
}
