package com.sigmundgranaas.forgero.smithing.networking.C2S;

import com.sigmundgranaas.forgero.smithing.block.entity.custom.SmithingAnvilBlockEntity;
import com.sigmundgranaas.forgero.smithing.networking.ModMessages;
import com.sigmundgranaas.forgero.smithing.networking.SmithingPacketValidator;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class SchematicSelectionC2SPacket {
	private SchematicSelectionC2SPacket() {
	}

	public static void register() {
		register(ModMessages.SCHEMATIC_SELECTED);
	}

	public static void register(Identifier id) {
		ServerPlayNetworking.registerGlobalReceiver(
				id,
				(server, player, handler, buf, responseSender) -> {
					BlockPos pos = buf.readBlockPos();
					Identifier selected = buf.readIdentifier();

					server.execute(() -> handle(player, pos, selected));
				}
		);
	}

	private static void handle(
			ServerPlayerEntity player,
			BlockPos pos,
			Identifier selected
	) {
		SmithingAnvilBlockEntity anvil =
				SmithingPacketValidator.getValidSmithingAnvil(player, pos);

		if (anvil == null) {
			return;
		}

		if (!SmithingPacketValidator.canSelectSchematicProduct(player, anvil, selected)) {
			return;
		}

		anvil.setPlannedProduct(selected);
	}
}
