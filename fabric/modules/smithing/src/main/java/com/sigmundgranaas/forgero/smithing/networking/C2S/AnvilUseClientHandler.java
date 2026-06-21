package com.sigmundgranaas.forgero.smithing.networking.C2S;

import com.sigmundgranaas.forgero.smithing.block.entity.custom.SmithingAnvilBlockEntity;
import com.sigmundgranaas.forgero.smithing.item.custom.MorphedItem;
import com.sigmundgranaas.forgero.smithing.item.custom.SmithingTongsItem;
import com.sigmundgranaas.forgero.smithing.networking.ModMessages;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureRules;

import net.minecraft.block.AnvilBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;

@Environment(EnvType.CLIENT)
public final class AnvilUseClientHandler {
	private AnvilUseClientHandler() {
	}

	public static void register() {
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (!world.isClient) {
				return ActionResult.PASS;
			}

			BlockPos pos = hitResult.getBlockPos();

			if (!(world.getBlockState(pos).getBlock() instanceof AnvilBlock)) {
				return ActionResult.PASS;
			}

			BlockEntity entity = world.getBlockEntity(pos);

			if (!(entity instanceof SmithingAnvilBlockEntity anvilEntity)) {
				return ActionResult.PASS;
			}

			ItemStack anvilStack = anvilEntity.getInventory().getStack(0);
			ItemStack handStack = player.getStackInHand(hand);

			boolean handEmpty = handStack.isEmpty();
			boolean anvilHasItem = !anvilStack.isEmpty();

			if (isTongsInteraction(handStack, anvilHasItem)) {
				sendAnvilUse(pos, hand);
				return ActionResult.SUCCESS;
			}

			if (handEmpty && anvilHasItem) {
				sendAnvilUse(pos, hand);
				return ActionResult.SUCCESS;
			}

			if (canSendPlaceAttempt(handStack)) {
				sendAnvilUse(pos, hand);
				return ActionResult.SUCCESS;
			}

			return ActionResult.PASS;
		});
	}

	private static boolean canSendPlaceAttempt(ItemStack handStack) {
		if (handStack.isEmpty()) {
			return false;
		}

		return handStack.getItem() instanceof MorphedItem
				|| TemperatureRules.canTrackTemperature(handStack);
	}

	private static boolean isTongsInteraction(ItemStack handStack, boolean anvilHasItem) {
		if (!(handStack.getItem() instanceof SmithingTongsItem)) {
			return false;
		}

		return anvilHasItem || SmithingTongsItem.hasStoredStack(handStack);
	}

	private static void sendAnvilUse(BlockPos pos, Hand hand) {
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeBlockPos(pos);
		buf.writeEnumConstant(hand);

		ClientPlayNetworking.send(ModMessages.ANVIL_SHIFT_USE, buf);
	}
}
