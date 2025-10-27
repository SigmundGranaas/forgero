package com.sigmundgranaas.forgero.smithing.temperature;

import com.sigmundgranaas.forgero.smithing.networking.S2C.TemperatureSyncS2CPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class TemperatureHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("ForgeroTemperature");
    private static final int FLUID_COOL_PER_TICK = 20;
    private static int tickCounter = 0;
    private static final int TICK_INTERVAL = 20;
    private static final int INVENTORY_COOL_PER_TICK = 1;
    private static final int FLAME_SOUND_INTERVAL = 2; // Play sound every 2 world ticks (~0.1s)

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(TemperatureHandler::onWorldTick);
    }

    private static void onWorldTick(ServerWorld world) {
        tickCounter++;
        if (tickCounter % TICK_INTERVAL != 0) {
            return;
        }
        for (ServerPlayerEntity player : world.getPlayers()) {
            boolean tookHeatDamage = false;
            for (int i = 0; i < player.getInventory().size(); i++) {
                ItemStack stack = player.getInventory().getStack(i);
                if (!TemperatureUtils.hasMaxTemperature(stack)) {
                    continue;
                }
                int temp = TemperatureUtils.getTemperature(stack);
                if (temp > 100) {
                    tookHeatDamage = true;
                }
                if (temp > 20) {
                    temp = Math.max(20, temp - INVENTORY_COOL_PER_TICK);
                    TemperatureUtils.setTemperature(stack, temp);
                }
            }
            if (tookHeatDamage) {
                player.damage(world.getDamageSources().hotFloor(), 1.0F);
            }
        }
        for (var entity : world.iterateEntities()) {
            if (!(entity instanceof ItemEntity itemEntity)) continue;
            ItemStack stack = itemEntity.getStack();
            if (!TemperatureUtils.hasMaxTemperature(stack)) {
                continue;
            }
            int temp = TemperatureUtils.getTemperature(stack);
            if (temp > 20) {
                temp = Math.max(20, temp - INVENTORY_COOL_PER_TICK);
                TemperatureUtils.setTemperature(stack, temp);
            }
            BlockPos pos = itemEntity.getBlockPos();
            boolean changed = false;
            boolean inFilledCauldron = TemperatureUtils.isItemInFilledWaterCauldron(itemEntity, world);

            // Emit flames when in very hot stage
            int maxTemp = TemperatureUtils.getMaxTemp(stack);
            if (TemperatureColorProvider.isInVeryHot(temp, maxTemp)) {
                world.spawnParticles(net.minecraft.particle.ParticleTypes.FLAME,
                    itemEntity.getX(), itemEntity.getY() + 0.3, itemEntity.getZ(),
                    3, 0.1, 0.2, 0.1, 0.05);

                // Play sizzling sound periodically
                if (tickCounter % FLAME_SOUND_INTERVAL == 0) {
                    world.playSound(null, pos, net.minecraft.sound.SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(),
                        net.minecraft.sound.SoundCategory.BLOCKS, 1.0F, 0.9F + (float)Math.random() * 0.3F);
                }
            }

            if (inFilledCauldron) {
                if (temp > 100) {
                    world.spawnParticles(net.minecraft.particle.ParticleTypes.CLOUD, itemEntity.getX(), itemEntity.getY() + 0.2, itemEntity.getZ(), 8, 0.2, 0.1, 0.2, 0.01);
                    world.playSound(null, pos, net.minecraft.sound.SoundEvents.BLOCK_FIRE_EXTINGUISH, net.minecraft.sound.SoundCategory.BLOCKS, 0.7F, 1.2F);
                }
                if (temp > 20) {
                    temp = Math.max(20, temp - FLUID_COOL_PER_TICK);
                    TemperatureUtils.setTemperature(stack, temp);
                    changed = true;
                }
            }
            if (changed) {
                TemperatureSyncS2CPacket.sendToClient(itemEntity, temp);
            }
        }
    }
}
