package com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.makeshift;

import com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.AssemblyStationScreenHandler;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandlerType;

public class MakeshiftAssemblyStationScreenHandler extends AssemblyStationScreenHandler {
	public static ScreenHandlerType<AssemblyStationScreenHandler> MAKESHIFT_ASSEMBLY_STATION_SCREEN_HANDLER = new ScreenHandlerType<>(MakeshiftAssemblyStationScreenHandler::new);

	public MakeshiftAssemblyStationScreenHandler(int syncId, PlayerInventory playerInventory) {
		super(syncId, playerInventory);
	}
}
