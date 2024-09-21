package com.sigmundgranaas.forgerofabric.gametest;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;

import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import org.jetbrains.annotations.NotNull;

public class EmptyTest {
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "test_run")
	public void testRuns(@NotNull TestContext context) {
		context.complete();
	}
}
