package com.sigmundgranaas.forgero.smithing.networking.S2C;

import com.sigmundgranaas.forgero.smithing.block.entity.custom.SmithingAnvilBlockEntity;
import com.sigmundgranaas.forgero.smithing.item.custom.MorphedItem;
import com.sigmundgranaas.forgero.smithing.minigame.MinigameLogic;
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
	public static void receive(@NotNull MinecraftClient client, @SuppressWarnings("unused") ClientPlayNetworkHandler handler, @NotNull PacketByteBuf buf, @SuppressWarnings("unused") PacketSender responseSender) {
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

		int coolingMarkerCount = buf.readInt();
		int[] coolingMarkerIndices = new int[coolingMarkerCount];
		for (int i = 0; i < coolingMarkerCount; i++) {
			coolingMarkerIndices[i] = buf.readInt();
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

			if (itemStacks.length > 0
					&& !itemStacks[0].isEmpty()
					&& itemStacks[0].getItem() instanceof MorphedItem) {
				int requiredHits = MinigameLogic.getRequiredHits(itemStacks[0]);
				double progress = Math.min(
						1.0,
						(double) markerHitsCount / requiredHits
				);

				MorphedItem.setMorphProgress(itemStacks[0], progress);
				itemStacks[0].getOrCreateNbt().putDouble("morphProgress", progress);
			}

			for (int i = 0; i < itemStacks.length; i++) {
				anvilEntity.getInventory().setStack(i, itemStacks[i]);
			}

			if (itemStacks.length > 0) {
				anvilEntity.getMinigameLogic().refreshRequiredHits(itemStacks[0]);
			}

			// Update marker state through the block entity's getter methods
			anvilEntity.getMarkerPositions().clear();
			anvilEntity.getMarkerHits().clear();
			for (int i = 0; i < markerCount; i++) {
				anvilEntity.getMarkerPositions().add(markerPositions[i]);
				anvilEntity.getMarkerHits().add(markerHits[i]);
			}

			// Update cooling marker indices through the block entity
			anvilEntity.getCoolingMarkerIndices().clear();
			for (int index : coolingMarkerIndices) {
				anvilEntity.getCoolingMarkerIndices().add(index);
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
