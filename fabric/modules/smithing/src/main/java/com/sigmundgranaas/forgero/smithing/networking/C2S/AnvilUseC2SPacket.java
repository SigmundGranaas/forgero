package com.sigmundgranaas.forgero.smithing.networking.C2S;

import com.sigmundgranaas.forgero.smithing.block.entity.custom.SmithingAnvilBlockEntity;
import com.sigmundgranaas.forgero.smithing.item.custom.MorphedItem;
import com.sigmundgranaas.forgero.smithing.networking.ModMessages;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class AnvilUseC2SPacket {
	private AnvilUseC2SPacket() {
	}

	public static void register() {
		ServerPlayNetworking.registerGlobalReceiver(
				ModMessages.ANVIL_SHIFT_USE,
				(server, player, handler, buf, responseSender) -> {
					BlockPos pos = buf.readBlockPos();
					Hand hand = buf.readEnumConstant(Hand.class);

					server.execute(() -> handle(player, pos, hand));
				}
		);
	}

	private static void handle(ServerPlayerEntity player, BlockPos pos, Hand hand) {
		BlockEntity entity = player.getWorld().getBlockEntity(pos);

		if (!(entity instanceof SmithingAnvilBlockEntity anvilEntity)) {
			return;
		}

		ItemStack stackInHand = player.getStackInHand(hand);
		ItemStack anvilItem = anvilEntity.getInventory().getStack(0);

		if (stackInHand.isEmpty() && !anvilItem.isEmpty()) {
			anvilEntity.tryPickupItem(player);
			return;
		}

		if (!anvilItem.isEmpty()) {
			return;
		}

		if (canBePlacedOnSmithingAnvil(stackInHand)) {
			anvilEntity.tryPlaceItem(player, hand);
		}
	}

	private static boolean canBePlacedOnSmithingAnvil(ItemStack stack) {
		return stack.getItem() instanceof MorphedItem
				|| TemperatureUtils.hasMaxTemperature(stack);
	}
}
