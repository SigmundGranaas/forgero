package com.sigmundgranaas.forgero.smithing.networking.C2S;

import com.sigmundgranaas.forgero.smithing.block.entity.custom.SmithingAnvilBlockEntity;
import com.sigmundgranaas.forgero.smithing.item.custom.SmithingTongsItem;
import com.sigmundgranaas.forgero.smithing.networking.ModMessages;
import com.sigmundgranaas.forgero.smithing.networking.SmithingPacketValidator;

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
		if (!SmithingPacketValidator.isValidHand(hand)) {
			return;
		}

		SmithingAnvilBlockEntity anvil =
				SmithingPacketValidator.getValidSmithingAnvil(player, pos);

		if (anvil == null) {
			return;
		}

		ItemStack stackInHand = player.getStackInHand(hand);
		ItemStack anvilStack = anvil.getInventory().getStack(0);

		if (stackInHand.getItem() instanceof SmithingTongsItem) {
			anvil.tryUseTongs(player, hand);
			return;
		}

		if (SmithingPacketValidator.canPickupFromAnvil(stackInHand, anvilStack)) {
			anvil.tryPickupItem(player);
			return;
		}

		if (SmithingPacketValidator.canPlaceOnAnvil(stackInHand, anvilStack)) {
			anvil.tryPlaceItem(player, hand);
		}
	}
}
