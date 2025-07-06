package com.sigmundgranaas.forgero.smithing.networking.packet;

import com.sigmundgranaas.forgero.smithing.block.entity.SmithingAnvilBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.fabric.api.networking.v1.PacketSender;

public class ItemStackSyncS2CPacket {
	@SuppressWarnings("unused")
	public static void receive(@NotNull MinecraftClient client, ClientPlayNetworkHandler handler, @NotNull PacketByteBuf buf, PacketSender responseSender) {
		int inventorySize = buf.readInt();
		@NotNull ItemStack[] itemStacks = new ItemStack[inventorySize];
		for (int i = 0; i < inventorySize; i++) {
			itemStacks[i] = buf.readItemStack();
		}
		@NotNull SimpleInventory inventory = new SimpleInventory(itemStacks);

		@NotNull BlockPos position = buf.readBlockPos();

		@Nullable var clientWorld = client.world;
		if (clientWorld == null || !(clientWorld.getBlockEntity(position) instanceof SmithingAnvilBlockEntity smithingAnvilBlockEntity)) {
			return;
		}

		smithingAnvilBlockEntity.getInventory().clear();
		for (int i = 0; i < inventory.size(); i++) {
			smithingAnvilBlockEntity.getInventory().setStack(i, inventory.getStack(i));
		}

		// --- Read marker positions and hits from packet ---
		int markerCount = buf.readInt();
		smithingAnvilBlockEntity.getMarkerPositions().clear();
		smithingAnvilBlockEntity.getMarkerHits().clear();
		for (int i = 0; i < markerCount; i++) {
			float x = buf.readFloat();
			float y = buf.readFloat();
			boolean hit = buf.readBoolean();
			smithingAnvilBlockEntity.getMarkerPositions().add(new net.minecraft.util.math.Vec2f(x, y));
			smithingAnvilBlockEntity.getMarkerHits().add(hit);
		}
	}
}
