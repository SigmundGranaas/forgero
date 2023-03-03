package com.sigmundgranaas.forgerofabric.gametest;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;

import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;


public class EmptyTest {

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "test_run", required = true)
	public void testRuns(TestContext context) {
		context.complete();
	}
}
