package com.sigmundgranaas.forgero.smithing.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.sigmundgranaas.forgero.smithing.networking.ModMessages;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class SchematicResultUtil {
	public static void openSchematicSelection(
			PlayerEntity player,
			BlockPos pos,
			List<Identifier> options,
			World world
	) {
		Map<Identifier, Integer> costs = new LinkedHashMap<>();

		for (Identifier id : options) {
			costs.put(id, SchematicMaterialCost.getCost(id));
		}

		openSchematicSelection(player, pos, options, costs, 0, world);
	}

	public static void openSchematicSelection(
			PlayerEntity player,
			BlockPos pos,
			List<Identifier> options,
			Map<Identifier, Integer> costs,
			int materialCount,
			World world
	) {
		if (world == null || world.isClient) {
			return;
		}

		if (options.isEmpty()) {
			player.sendMessage(Text.literal("You have no schematics for this material."), true);
			return;
		}

		PacketByteBuf data = PacketByteBufs.create();

		data.writeBlockPos(pos);
		data.writeInt(materialCount);

		data.writeInt(options.size());

		for (Identifier id : options) {
			data.writeIdentifier(id);
			data.writeInt(costs.getOrDefault(id, SchematicMaterialCost.getCost(id)));
		}

		ServerPlayNetworking.send(
				(net.minecraft.server.network.ServerPlayerEntity) player,
				ModMessages.OPEN_SCHEMATIC_SELECTION,
				data
		);
	}

	public static List<Identifier> findAvailableSchematicProductsForPlayer(PlayerEntity player) {
		List<Identifier> result = new ArrayList<>();
		var inv = player.getInventory();

		for (int i = 0; i < inv.size(); i++) {
			ItemStack s = inv.getStack(i);

			if (s.isEmpty()) {
				continue;
			}

			deriveProductIdFromSchematic(s).ifPresent(id -> {
				if (!result.contains(id)) {
					result.add(id);
				}
			});
		}

		return result;
	}

	public static Optional<Identifier> deriveProductIdFromSchematic(ItemStack schematicStack) {
		String key = schematicStack.getItem().getTranslationKey();

		if (key.endsWith("-schematic")) {
			String base = key.substring(0, key.length() - "-schematic".length());

			int nsIdx = base.indexOf('.');

			if (nsIdx >= 0 && nsIdx < base.length() - 1) {
				String afterPrefix = base.substring(nsIdx + 1);

				int typeIdx = afterPrefix.indexOf('.');

				if (typeIdx >= 0 && typeIdx < afterPrefix.length() - 1) {
					String namespace = afterPrefix.substring(0, typeIdx);
					String path = afterPrefix.substring(typeIdx + 1);

					try {
						return Optional.of(new Identifier(namespace, path));
					} catch (Exception ignored) {
					}
				}
			}
		}

		return Optional.empty();
	}
}
