package com.sigmundgranaas.forgero.smithing.networking.C2S;

import com.sigmundgranaas.forgero.smithing.block.entity.custom.SmithingAnvilBlockEntity;
import com.sigmundgranaas.forgero.smithing.networking.ModMessages;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class SchematicSelectionC2SPacket {
	private SchematicSelectionC2SPacket() {
	}

	public static void register() {
		ServerPlayNetworking.registerGlobalReceiver(
				ModMessages.SCHEMATIC_SELECTED,
				(server, player, handler, buf, responseSender) -> {
					BlockPos pos = buf.readBlockPos();
					Identifier selected = buf.readIdentifier();

					server.execute(() -> {
						BlockEntity be = player.getWorld().getBlockEntity(pos);

						if (be instanceof SmithingAnvilBlockEntity anvil) {
							anvil.setPlannedProduct(selected);
						}
					});
				}
		);
	}
}
