package com.sigmundgranaas.forgero.smithing.networking;


import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.smithing.networking.C2S.AnvilUseC2SPacket;
import com.sigmundgranaas.forgero.smithing.networking.C2S.SchematicSelectionC2SPacket;
import com.sigmundgranaas.forgero.smithing.networking.S2C.AnvilSyncS2CPacket;
import com.sigmundgranaas.forgero.smithing.networking.S2C.HearthBlockSyncS2CPacket;
import com.sigmundgranaas.forgero.smithing.networking.S2C.SchematicSelectionS2CPacket;
import com.sigmundgranaas.forgero.smithing.networking.S2C.TemperatureSyncS2CPacket;

import net.minecraft.util.Identifier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class ModMessages {

	public static final Identifier ITEM_SYNC = new Identifier(Forgero.NAMESPACE, "item_sync");
	public static final Identifier OPEN_SCHEMATIC_SELECTION = new Identifier(Forgero.NAMESPACE, "open_schematic_selection");
	public static final Identifier SCHEMATIC_SELECTED = new Identifier(Forgero.NAMESPACE, "schematic_selected");
	public static final Identifier ANVIL_SHIFT_USE = new Identifier(Forgero.NAMESPACE, "anvil_shift_use");
	public static final Identifier TEMPERATURE_SYNC = new Identifier(Forgero.NAMESPACE, "temperature_sync");
	public static final Identifier HEART_BLOCK_SYNC = new Identifier(Forgero.NAMESPACE, "heart_block_sync");

	private static volatile boolean C2S_REGISTERED = false;
	static {
		registerC2SPackets();
	}

	@Environment(EnvType.CLIENT)
	public static void registerS2CPackets() {
		ClientPlayNetworking.registerGlobalReceiver(ITEM_SYNC, AnvilSyncS2CPacket::receive);
		ClientPlayNetworking.registerGlobalReceiver(OPEN_SCHEMATIC_SELECTION, SchematicSelectionS2CPacket::receive);
		ClientPlayNetworking.registerGlobalReceiver(TEMPERATURE_SYNC, TemperatureSyncS2CPacket::receive);
		ClientPlayNetworking.registerGlobalReceiver(HEART_BLOCK_SYNC, (client, handler, buf, responseSender) -> {
			HearthBlockSyncS2CPacket.receive(client, buf);
		});
	}

	public static void registerC2SPackets() {
		if (C2S_REGISTERED) return;
		C2S_REGISTERED = true;
		SchematicSelectionC2SPacket.register(SCHEMATIC_SELECTED);
		AnvilUseC2SPacket.register();
		AnvilUseC2SPacket.registerServer();
	}
}
