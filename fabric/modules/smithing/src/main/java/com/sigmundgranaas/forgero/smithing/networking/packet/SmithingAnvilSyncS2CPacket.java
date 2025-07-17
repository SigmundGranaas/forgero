package com.sigmundgranaas.forgero.smithing.networking.packet;

import com.sigmundgranaas.forgero.smithing.block.entity.SmithingAnvilBlockEntity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.networking.v1.PacketSender;

import org.jetbrains.annotations.NotNull;

public class SmithingAnvilSyncS2CPacket {

	public static void receive(@NotNull MinecraftClient client, ClientPlayNetworkHandler handler, @NotNull PacketByteBuf buf, PacketSender responseSender) {
		// Read data in the same order it was written by the server
		BlockPos position = buf.readBlockPos();
		int inventorySize = buf.readInt();
		ItemStack[] itemStacks = new ItemStack[inventorySize];
		for (int i = 0; i < inventorySize; i++) {
			itemStacks[i] = buf.readItemStack();
		}

		int markerCount = buf.readInt();
		Vec2f[] markerPositions = new Vec2f[markerCount];
		boolean[] markerHits = new boolean[markerCount];
		for (int i = 0; i < markerCount; i++) {
			markerPositions[i] = new Vec2f(buf.readFloat(), buf.readFloat());
			markerHits[i] = buf.readBoolean();
		}

		int fastMarkerCount = buf.readInt();
		int[] fastMarkerIndices = new int[fastMarkerCount];
		for (int i = 0; i < fastMarkerCount; i++) {
			fastMarkerIndices[i] = buf.readInt();
		}

		int markerAttempts = buf.readInt();
		int markerHitsCount = buf.readInt();


		client.execute(() -> {
			// All logic that interacts with the world must be executed on the client thread
			World world = client.world;
			if (world == null || !(world.getBlockEntity(position) instanceof SmithingAnvilBlockEntity anvilEntity)) {
				return;
			}

			// Update inventory
			anvilEntity.getInventory().clear();
			for (int i = 0; i < itemStacks.length; i++) {
				anvilEntity.getInventory().setStack(i, itemStacks[i]);
			}

			// Update marker state
			anvilEntity.getMarkerPositions().clear();
			anvilEntity.getMarkerHits().clear();
			for (int i = 0; i < markerCount; i++) {
				anvilEntity.getMarkerPositions().add(markerPositions[i]);
				anvilEntity.getMarkerHits().add(markerHits[i]);
			}

			// Update fast marker indices
			anvilEntity.getFastMarkerIndices().clear();
			for (int index : fastMarkerIndices) {
				anvilEntity.getFastMarkerIndices().add(index);
			}

			// Update progress
			anvilEntity.setMarkerAttempts(markerAttempts);
			anvilEntity.setMarkerHitsCount(markerHitsCount);
		});
	}
}
