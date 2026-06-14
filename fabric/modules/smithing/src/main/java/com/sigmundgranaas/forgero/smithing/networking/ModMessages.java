package com.sigmundgranaas.forgero.smithing.networking;

import com.sigmundgranaas.forgero.core.Forgero;
import net.minecraft.util.Identifier;

/**
 * Packet identifiers only.
 *
 * Keep this class common-safe:
 * - no ClientPlayNetworking
 * - no ServerPlayNetworking
 * - no static registration block
 */
public final class ModMessages {
	private ModMessages() {
	}

	public static final Identifier ITEM_SYNC =
			new Identifier(Forgero.NAMESPACE, "item_sync");

	public static final Identifier OPEN_SCHEMATIC_SELECTION =
			new Identifier(Forgero.NAMESPACE, "open_schematic_selection");

	public static final Identifier SCHEMATIC_SELECTED =
			new Identifier(Forgero.NAMESPACE, "schematic_selected");

	public static final Identifier ANVIL_SHIFT_USE =
			new Identifier(Forgero.NAMESPACE, "anvil_shift_use");

	public static final Identifier TEMPERATURE_SYNC =
			new Identifier(Forgero.NAMESPACE, "temperature_sync");

	/**
	 * Java name fixed to HEARTH, but packet id kept as "heart_block_sync"
	 * so existing saves/packet senders do not break.
	 */
	public static final Identifier HEARTH_BLOCK_SYNC =
			new Identifier(Forgero.NAMESPACE, "hearth_block_sync");

}
