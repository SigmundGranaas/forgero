package com.sigmundgranaas.forgero.smithing.networking.S2C;

import com.sigmundgranaas.forgero.smithing.block.entity.custom.HearthBlockEntity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class HeartBlockSyncS2CPacket {
    private final BlockPos pos;
    private final ItemStack stack;

    public HeartBlockSyncS2CPacket(BlockPos pos, ItemStack stack) {
        this.pos = pos;
        this.stack = stack;
    }

    public static void send(HearthBlockEntity entity) {
        if (entity.getWorld() == null || entity.getWorld().isClient) return;

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(entity.getPos());
        buf.writeItemStack(entity.getStack(0));

        for (ServerPlayerEntity player : PlayerLookup.tracking((net.minecraft.server.world.ServerWorld) entity.getWorld(), entity.getPos())) {
            ServerPlayNetworking.send(player, com.sigmundgranaas.forgero.smithing.networking.ModMessages.HEART_BLOCK_SYNC, buf);
        }
    }

    public static void receive(MinecraftClient client, HeartBlockSyncS2CPacket packet) {
        client.execute(() -> {
            if (client.world != null) {
                if (client.world.getBlockEntity(packet.pos) instanceof HearthBlockEntity hearth) {
                    hearth.setStack(0, packet.stack);
                    hearth.markDirty();
                    hearth.requestModelDataRefresh();
                }
            }
        });
    }

    public static void receive(MinecraftClient client, PacketByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        ItemStack stack = buf.readItemStack();
        receive(client, new HeartBlockSyncS2CPacket(pos, stack));
    }
}
