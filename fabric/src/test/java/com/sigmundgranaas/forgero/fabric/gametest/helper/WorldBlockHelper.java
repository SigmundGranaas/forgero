package com.sigmundgranaas.forgero.fabric.gametest.helper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import com.sigmundgranaas.forgero.testutil.ContextSupplier;
import com.sigmundgranaas.forgero.testutil.TestPos;
import com.sigmundgranaas.forgero.testutil.TestPosCollection;

import net.minecraft.block.Block;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class WorldBlockHelper implements ContextSupplier {
	private final TestContext context;

	public WorldBlockHelper(TestContext context) {
		this.context = context;
	}

	public void replace(Block from, Block to) {
		Map<Block, Block> replacement = new HashMap<>();
		replacement.put(from, to);
		replace(replacement);
	}

	public void replace(Map<Block, Block> replacements) {
		testCollection().positions()
				.forEach(pos -> {
					Block block = context.getWorld().getBlockState(pos.absolute()).getBlock();
					if (replacements.containsKey(block)) {
						context.getWorld().setBlockState(pos.absolute(), replacements.get(block).getDefaultState());
					}
				});
	}

	public TestPosCollection testCollection() {
		return of(context.getTestBox());
	}

	public TestPosCollection of(Box testBounds) {
		Set<TestPos> positions = new HashSet<>();
		for (double x = testBounds.minX; x <= testBounds.maxX; x++) {
			for (double y = testBounds.minY; y <= testBounds.maxY; y++) {
				for (double z = testBounds.minZ; z <= testBounds.maxZ; z++) {
					BlockPos absolute = new BlockPos((int) x, (int) y, (int) z);
					BlockPos relative = context.getRelativePos(absolute);
					positions.add(TestPos.of(absolute, relative));
				}
			}
		}
		return TestPosCollection.of(positions);
	}

	public static Predicate<Block> is(Block block) {
		return (b) -> b == block;
	}

	public static Predicate<TestPos> is(Block block, ContextSupplier supplier) {
		return (pos) -> supplier.absolute(pos).getBlock() == block;
	}

	@Override
	public TestContext get() {
		return context;
	}
}
