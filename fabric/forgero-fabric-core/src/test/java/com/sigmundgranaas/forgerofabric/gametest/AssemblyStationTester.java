package com.sigmundgranaas.forgerofabric.gametest;

import java.util.function.Supplier;

import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.AssemblyStationScreenHandler;
import com.sigmundgranaas.forgero.minecraft.common.conversion.CachedConverter;

import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;

public class AssemblyStationTester {
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "assembly_station_test", required = true)
	public void unableToDisassembleDamagedTool(TestContext context) {
		var player = context.createMockPlayer();
		var state = ForgeroStateRegistry.STATES.find("forgero:diamond-pickaxe").map(Supplier::get);
		if (state.isEmpty()) {
			throw new GameTestException("Unable to find item");
		}
		var tool = CachedConverter.of(state.get());
		tool.getOrCreateNbt().putInt("Damage", 1);

		var screenHandler = new AssemblyStationScreenHandler(1, player.getInventory());
		if (screenHandler.getSlot(0).canInsert(tool)) {
			throw new GameTestException("Can dissasemble damaged tools!");
		}
		context.complete();
	}

}
