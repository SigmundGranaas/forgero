package com.sigmundgranaas.forgero.smithing.networking.packet;

import com.sigmundgranaas.forgero.smithing.block.entity.BloomeryExtensionBlockEntity;
import org.jetbrains.annotations.NotNull;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.networking.v1.PacketSender;

public class BloomeryExtensionSyncS2CPacket {
	public static void receive(@NotNull MinecraftClient client, ClientPlayNetworkHandler handler, @NotNull PacketByteBuf buf, PacketSender responseSender) {
		BlockPos position = buf.readBlockPos();
		int inventorySize = buf.readInt();
		DefaultedList<ItemStack> inventory = DefaultedList.ofSize(inventorySize, ItemStack.EMPTY);
		for (int i = 0; i < inventorySize; i++) {
			inventory.set(i, buf.readItemStack());
		}

		client.execute(() -> {
			World world = client.world;
			if (world == null || !(world.getBlockEntity(position) instanceof BloomeryExtensionBlockEntity bloomeryEntity)) {
				return;
			}

			// Update inventory on the client
			DefaultedList<ItemStack> clientInventory = bloomeryEntity.getInventory();
			for (int i = 0; i < inventory.size(); i++) {
				clientInventory.set(i, inventory.get(i));
			}
		});
	}
}

