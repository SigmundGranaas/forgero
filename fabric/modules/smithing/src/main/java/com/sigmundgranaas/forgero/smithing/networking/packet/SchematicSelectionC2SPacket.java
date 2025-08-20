package com.sigmundgranaas.forgero.smithing.networking.packet;

import com.sigmundgranaas.forgero.smithing.block.entity.SmithingAnvilBlockEntity;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class SchematicSelectionC2SPacket {

	public static void register(Identifier id) {
		ServerPlayNetworking.registerGlobalReceiver(id, (server, player, handler, buf, responseSender) -> {
			BlockPos pos = buf.readBlockPos();
			Identifier selected = buf.readIdentifier();
			server.execute(() -> {
				var be = player.getWorld().getBlockEntity(pos);
				if (be instanceof SmithingAnvilBlockEntity anvil) {
					anvil.setPlannedProduct(selected);
				}
			});
		});
	}
}
