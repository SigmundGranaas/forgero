package com.sigmundgranaas.forgero.smithing.temperature;

import com.sigmundgranaas.forgero.smithing.item.custom.SmithingTongsItem;
import com.sigmundgranaas.forgero.smithing.networking.S2C.TemperatureSyncS2CPacket;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureRules.TemperatureStage;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureRules.TemperatureStages;

import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class TemperatureHandler {
    private static final int FLUID_COOL_PER_TICK = 20;
    private static int tickCounter = 0;
    private static final int TICK_INTERVAL = 20;
    private static final int INVENTORY_COOL_PER_TICK = 1;

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
                if (stack.isEmpty()) {
                    continue;
                }

                if (stack.getItem() instanceof SmithingTongsItem) {
                    coolStoredTongsStack(stack);
                    continue;
                }

                if (!TemperatureRules.canTrackTemperature(stack)) {
                    continue;
                }
                int temp = TemperatureState.currentTemperature(stack);
                if (temp > 100) {
                    tookHeatDamage = true;
                }
                if (temp > TemperatureState.DEFAULT_TEMPERATURE) {
                    temp = Math.max(TemperatureState.DEFAULT_TEMPERATURE, temp - INVENTORY_COOL_PER_TICK);
                    TemperatureRules.setTemperature(stack, temp);
                }
            }
            if (tookHeatDamage) {
                player.damage(world.getDamageSources().hotFloor(), 1.0F);
            }
        }
        for (var entity : world.iterateEntities()) {
            if (!(entity instanceof ItemEntity itemEntity)) continue;
            ItemStack stack = itemEntity.getStack();
            if (stack.isEmpty()) {
                continue;
            }

            if (!TemperatureRules.canTrackTemperature(stack)) {
                continue;
            }
            int temp = TemperatureState.currentTemperature(stack);
            if (temp > TemperatureState.DEFAULT_TEMPERATURE) {
                temp = Math.max(TemperatureState.DEFAULT_TEMPERATURE, temp - INVENTORY_COOL_PER_TICK);
                TemperatureRules.setTemperature(stack, temp);
            }
            BlockPos pos = itemEntity.getBlockPos();
            boolean changed = false;
            boolean inFilledCauldron = TemperatureRules.isItemInFilledWaterCauldron(itemEntity, world);

            TemperatureStages stages = TemperatureRules.stages(stack);
            // emitTemperatureEffects(world, itemEntity, pos, temp, stages);

            if (inFilledCauldron) {
                if (temp > 100) {
                    world.spawnParticles(ParticleTypes.CLOUD, itemEntity.getX(), itemEntity.getY() + 0.2, itemEntity.getZ(), 8, 0.2, 0.1, 0.2, 0.01);
                    world.playSound(null, pos, net.minecraft.sound.SoundEvents.BLOCK_FIRE_EXTINGUISH, net.minecraft.sound.SoundCategory.BLOCKS, 0.7F, 1.2F);
                }
                if (temp > TemperatureState.DEFAULT_TEMPERATURE) {
                    temp = Math.max(TemperatureState.DEFAULT_TEMPERATURE, temp - FLUID_COOL_PER_TICK);
                    TemperatureRules.quench(stack, temp);
                    changed = true;
                }
            }
            if (changed) {
                TemperatureSyncS2CPacket.sendToClient(itemEntity, temp);
            }
        }
    }

    private static void coolStoredTongsStack(ItemStack tongsStack) {
        ItemStack stored = SmithingTongsItem.getStoredStack(tongsStack);

        if (stored.isEmpty() || !TemperatureRules.canTrackTemperature(stored)) {
            return;
        }

        int temp = TemperatureState.currentTemperature(stored);

        if (temp <= TemperatureState.DEFAULT_TEMPERATURE) {
            return;
        }

        TemperatureRules.setTemperature(stored, Math.max(TemperatureState.DEFAULT_TEMPERATURE, temp - INVENTORY_COOL_PER_TICK));
        SmithingTongsItem.setStoredStack(tongsStack, stored);
    }
}
