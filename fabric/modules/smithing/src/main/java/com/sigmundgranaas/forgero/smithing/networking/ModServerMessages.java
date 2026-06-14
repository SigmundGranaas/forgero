package com.sigmundgranaas.forgero.smithing.networking;

import com.sigmundgranaas.forgero.smithing.networking.C2S.AnvilUseC2SPacket;
import com.sigmundgranaas.forgero.smithing.networking.C2S.SchematicSelectionC2SPacket;

/**
 * Server/common packet receivers.
 *
 * This class must not import client networking classes.
 */
public final class ModServerMessages {
	private static boolean registered = false;

	private ModServerMessages() {
	}

	public static void registerC2SPackets() {
		if (registered) {
			return;
		}

		registered = true;

		SchematicSelectionC2SPacket.register();
		AnvilUseC2SPacket.register();
	}
}
