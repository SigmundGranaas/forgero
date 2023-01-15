package com.sigmundgranaas.forgerofabric.gametest;

import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.AssemblyStationScreenHandler;
import com.sigmundgranaas.forgero.minecraft.common.conversion.StateConverter;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;

import java.util.function.Supplier;

public class AssemblyStationTester {
    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "assembly_station_test", required = true)
    public void unableToDisassembleDamagedTool(TestContext context) {
        var player = context.createMockCreativePlayer();
        var state = ForgeroStateRegistry.STATES.find("forgero:diamond-pickaxe").map(Supplier::get);
        if (state.isEmpty()) {
            throw new GameTestException("Unable to find item");
        }
        var tool = StateConverter.of(state.get());
        tool.getOrCreateNbt().putInt("Damage", 1);

        var screenHandler = new AssemblyStationScreenHandler(1, player.getInventory());
        if (screenHandler.getSlot(0).canInsert(tool)) {
            throw new GameTestException("Can dissasemble damaged tools!");
        }
        if (!screenHandler.getSlot(1).canInsert(tool)) {
            throw new GameTestException("Cannot add tool to assembly inventory");
        }
        context.complete();
    }

}
