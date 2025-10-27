package com.sigmundgranaas.forgero.smithing.networking.S2C;

import com.sigmundgranaas.forgero.smithing.campfire.ExtraHeatSlotProvider;
import com.sigmundgranaas.forgero.smithing.networking.ModMessages;
import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.NotNull;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class CampfireSyncS2CPacket {
	public static void receive(@NotNull MinecraftClient client, @SuppressWarnings("unused") ClientPlayNetworkHandler handler, @NotNull PacketByteBuf buf, @SuppressWarnings("unused") PacketSender responseSender) {
		BlockPos pos = buf.readBlockPos();
		ItemStack itemStack = buf.readItemStack();

		client.execute(() -> {
			World world = client.world;
			if (world == null) return;

			BlockEntity be = world.getBlockEntity(pos);
			if (be instanceof CampfireBlockEntity campfire) {
				ExtraHeatSlotProvider provider = (ExtraHeatSlotProvider)(Object)campfire;
				provider.forgero$setExtraHeatSlotClient(itemStack);
			}
		});
	}

	public static void send(ServerPlayerEntity player, BlockPos pos, ItemStack itemStack) {
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeBlockPos(pos);
		buf.writeItemStack(itemStack);
		ServerPlayNetworking.send(player, ModMessages.CAMPFIRE_EXTRA_SLOT_SYNC, buf);
	}
}
