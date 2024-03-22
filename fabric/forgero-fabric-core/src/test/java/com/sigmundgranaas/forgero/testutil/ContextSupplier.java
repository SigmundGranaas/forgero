package com.sigmundgranaas.forgero.testutil;

import java.util.function.Supplier;

import net.minecraft.block.BlockState;
import net.minecraft.test.TestContext;

public interface ContextSupplier extends Supplier<TestContext> {
	static ContextSupplier of(TestContext ctx) {
		return () -> ctx;
	}

	default BlockState relative(TestPos pos) {
		return get().getBlockState(pos.relative());
	}

	default BlockState absolute(TestPos pos) {
		return get().getWorld().getBlockState(pos.absolute());
	}
}
