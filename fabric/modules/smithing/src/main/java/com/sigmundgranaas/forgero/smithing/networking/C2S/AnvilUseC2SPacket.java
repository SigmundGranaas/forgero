package com.sigmundgranaas.forgero.smithing.networking.C2S;

import com.sigmundgranaas.forgero.smithing.block.entity.custom.SmithingAnvilBlockEntity;
import com.sigmundgranaas.forgero.smithing.networking.ModMessages;

import net.minecraft.block.Blocks;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class AnvilUseC2SPacket {
    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient && world.getBlockState(hitResult.getBlockPos()).getBlock() == Blocks.ANVIL) {
                // Always allow pickup if hand is empty and anvil has item
                boolean isSneaking = player.isSneaking();
                boolean isHandEmpty = player.getStackInHand(hand).isEmpty();
                BlockEntity entity = world.getBlockEntity(hitResult.getBlockPos());
                if (entity instanceof SmithingAnvilBlockEntity anvilEntity) {
                    ItemStack anvilItem = anvilEntity.getInventory().getStack(0);
                    ItemStack stackInHand = player.getStackInHand(hand);
                    if (isHandEmpty && !anvilItem.isEmpty()) {
                        PacketByteBuf buf = PacketByteBufs.create();
                        buf.writeBlockPos(hitResult.getBlockPos());
                        buf.writeEnumConstant(hand);
                        ClientPlayNetworking.send(ModMessages.ANVIL_SHIFT_USE, buf);
                        return ActionResult.SUCCESS;
                    }
                    // Only allow placing if sneaking and item has max temperature
                    if (isSneaking && !stackInHand.isEmpty() && com.sigmundgranaas.forgero.smithing.util.TemperatureItemUtil.hasMaxTemperature(stackInHand)) {
                        PacketByteBuf buf = PacketByteBufs.create();
                        buf.writeBlockPos(hitResult.getBlockPos());
                        buf.writeEnumConstant(hand);
                        ClientPlayNetworking.send(ModMessages.ANVIL_SHIFT_USE, buf);
                        return ActionResult.SUCCESS;
                    }
                }
            }
            return ActionResult.PASS;
        });
    }

    public static void registerServer() {
        ServerPlayNetworking.registerGlobalReceiver(ModMessages.ANVIL_SHIFT_USE, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            Hand hand = buf.readEnumConstant(Hand.class);
            server.execute(() -> {
                BlockEntity entity = player.getWorld().getBlockEntity(pos);
                if (entity instanceof SmithingAnvilBlockEntity anvilEntity) {
                    ItemStack stackInHand = player.getStackInHand(hand);
                    ItemStack anvilItem = anvilEntity.getInventory().getStack(0);
                    if (stackInHand.isEmpty() && !anvilItem.isEmpty()) {
                        // Pick up item from anvil if hand is empty and anvil has item
                        anvilEntity.tryPickupItem(player);
                    } else if (anvilItem.isEmpty() && com.sigmundgranaas.forgero.smithing.util.TemperatureItemUtil.hasMaxTemperature(stackInHand)) {
                        // Place item with max temperature if anvil is empty and hand has valid item
                        anvilEntity.tryPlaceItem(player, hand);
                    }
                }
            });
        });
    }
}
