package com.sigmundgranaas.forgero.testutil;

import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public record TestPos(BlockPos absoluteRoot, BlockPos relativeRoot, BlockPos offset) {
	public BlockPos relative() {
		return relativeRoot.add(offset);
	}

	public BlockPos absolute() {
		return absoluteRoot.add(offset);
	}

	public TestPos offset(BlockPos offset) {
		return new TestPos(absolute(), relative(), offset);
	}

	public static TestPos of(BlockPos relative, TestContext context) {
		return new TestPos(context.getAbsolutePos(relative), relative, BlockPos.ORIGIN);
	}

	public static TestPos of(BlockPos absolute, BlockPos relative) {
		return new TestPos(absolute, relative, BlockPos.ORIGIN);
	}

	public static TestPos of(TestPos pos, BlockPos offset) {
		return new TestPos(pos.absoluteRoot, pos.relativeRoot, offset);
	}
}
