package com.sigmundgranaas.forgero.smithing.networking.packet;

import com.sigmundgranaas.forgero.smithing.block.entity.SmithingAnvilBlockEntity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.fabric.api.networking.v1.PacketSender;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

		smithingAnvilBlockEntity.setInventory(inventory);
	}
}
