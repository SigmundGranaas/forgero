package com.sigmundgranaas.forgero.testutil;

import net.minecraft.test.TestContext;

public class GameTestAssertions implements ContextSupplier {
	private final TestContext context;

	public GameTestAssertions(TestContext context) {
		this.context = context;
	}

	@Override
	public TestContext get() {
		return context;
	}
}
