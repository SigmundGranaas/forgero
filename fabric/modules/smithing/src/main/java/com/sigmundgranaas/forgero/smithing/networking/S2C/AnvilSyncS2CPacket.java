package com.sigmundgranaas.forgero.smithing.networking.S2C;

import com.sigmundgranaas.forgero.smithing.block.entity.custom.SmithingAnvilBlockEntity;
import org.jetbrains.annotations.NotNull;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.networking.v1.PacketSender;

public class AnvilSyncS2CPacket {
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

		// NEW: read ingot crafting state
		boolean ingotCrafting = buf.readBoolean();
		boolean hasPlanned = buf.readBoolean();
		Identifier plannedProductId = hasPlanned ? buf.readIdentifier() : null;

		// NEW: one-shot final morph overlay flag
		boolean finalMorphOnce = buf.readBoolean();

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

			// Update ingot crafting state
			anvilEntity.clientSyncIngotState(ingotCrafting, plannedProductId);

			// Refresh start/result images on the client so renderer has them
			anvilEntity.clientRefreshMorphImages();

			// Trigger final morph overlay if requested
			if (finalMorphOnce) {
				anvilEntity.clientTriggerFinalMorphOnce();
			}

			// Force re-render
			var wr = client.worldRenderer;
			if (wr != null) {
				var state = world.getBlockState(position);
				wr.updateBlock(world, position, state, state, 3);
			}
		});
	}
}
