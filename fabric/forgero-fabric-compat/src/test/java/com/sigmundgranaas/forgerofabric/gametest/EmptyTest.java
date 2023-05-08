package com.sigmundgranaas.forgerofabric.gametest;

import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;


public class EmptyTest {
	@GameTest(structureName = FabricGameTest.EMPTY_STRUCTURE, batchId = "test_run", required = true)
	public void testRuns(TestContext context) {
		context.complete();
	}
}
