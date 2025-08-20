package com.sigmundgranaas.forgero.smithing.networking;


import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.smithing.networking.packet.HeartBlockSyncS2CPacket;
import com.sigmundgranaas.forgero.smithing.networking.packet.SchematicSelectionC2SPacket;
import com.sigmundgranaas.forgero.smithing.networking.packet.SchematicSelectionS2CPacket;
import com.sigmundgranaas.forgero.smithing.networking.packet.SmithingAnvilSyncS2CPacket;

import net.minecraft.util.Identifier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class ModMessages {

	public static final Identifier ITEM_SYNC = new Identifier(Forgero.NAMESPACE, "item_sync");
	public static final Identifier OPEN_SCHEMATIC_SELECTION = new Identifier(Forgero.NAMESPACE, "open_schematic_selection");
	public static final Identifier SCHEMATIC_SELECTED = new Identifier(Forgero.NAMESPACE, "schematic_selected");
	public static final Identifier HEART_BLOCK_SYNC = new Identifier(Forgero.NAMESPACE, "heart_block_sync");

	// Register C2S on class load (server + client). Safeguard with a flag to avoid duplicate registrations.
	private static volatile boolean C2S_REGISTERED = false;
	static {
		registerC2SPackets();
	}

	@Environment(EnvType.CLIENT)
	public static void registerS2CPackets() {
		ClientPlayNetworking.registerGlobalReceiver(ITEM_SYNC, SmithingAnvilSyncS2CPacket::receive);
		ClientPlayNetworking.registerGlobalReceiver(OPEN_SCHEMATIC_SELECTION, SchematicSelectionS2CPacket::receive);
		ClientPlayNetworking.registerGlobalReceiver(HEART_BLOCK_SYNC, (client, handler, buf, responseSender) -> {
			HeartBlockSyncS2CPacket.receive(client, buf);
		});
	}

	public static void registerC2SPackets() {
		if (C2S_REGISTERED) return;
		C2S_REGISTERED = true;
		SchematicSelectionC2SPacket.register(SCHEMATIC_SELECTED);
	}
}
