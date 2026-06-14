package com.sigmundgranaas.forgero.smithing.networking;

import com.sigmundgranaas.forgero.smithing.networking.S2C.AnvilSyncS2CPacket;
import com.sigmundgranaas.forgero.smithing.networking.S2C.HearthBlockSyncS2CPacket;
import com.sigmundgranaas.forgero.smithing.networking.S2C.SchematicSelectionS2CPacket;
import com.sigmundgranaas.forgero.smithing.networking.S2C.TemperatureSyncS2CPacket;
import com.sigmundgranaas.forgero.smithing.networking.C2S.AnvilUseClientHandler;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/**
 * Client-only networking.
 *
 * Only call this from ForgeroClientSmithingInitializer.
 */
@Environment(EnvType.CLIENT)
public final class ModClientMessages {
	private static boolean registered = false;

	private ModClientMessages() {
	}

	public static void registerClientPackets() {
		if (registered) {
			return;
		}

		registered = true;

		registerS2CPackets();
		registerClientPacketSenders();
	}

	private static void registerS2CPackets() {
		ClientPlayNetworking.registerGlobalReceiver(
				ModMessages.ITEM_SYNC,
				AnvilSyncS2CPacket::receive
		);

		ClientPlayNetworking.registerGlobalReceiver(
				ModMessages.OPEN_SCHEMATIC_SELECTION,
				SchematicSelectionS2CPacket::receive
		);

		ClientPlayNetworking.registerGlobalReceiver(
				ModMessages.TEMPERATURE_SYNC,
				TemperatureSyncS2CPacket::receive
		);

		ClientPlayNetworking.registerGlobalReceiver(
				ModMessages.HEARTH_BLOCK_SYNC,
				(client, handler, buf, responseSender) ->
						HearthBlockSyncS2CPacket.receive(client, buf)
		);
	}

	private static void registerClientPacketSenders() {
		AnvilUseClientHandler.register();
	}
}
