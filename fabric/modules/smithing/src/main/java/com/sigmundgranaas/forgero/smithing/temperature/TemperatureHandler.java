package com.sigmundgranaas.forgero.smithing.temperature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class TemperatureHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("ForgeroTemperature");
    private static final int HEAT_PER_TICK = 1;
    private static final int COOL_PER_TICK = 2;
    private static final int INVENTORY_COOL_PER_TICK = 1; // Slower cooling in inventory
    private static final int INVENTORY_COOL_TICK_INTERVAL = 20; // Only cool every 20 ticks
    private static int tickCounter = 0;
    private static final int TICK_INTERVAL = 20; // Only update every 20 ticks
    private static final int FLUID_COOL_PER_TICK = 20; // Cooling rate in fluid

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
                int prevTemp = temp;
                if (temp > 100) {
                    if (player.getMainHandStack() == stack || player.getOffHandStack() == stack) {
                        tookHeatDamage = true;
                    } else {
                        tookHeatDamage = true;
                    }
                }
                if (tickCounter % 20 == 0) { // Changed to every 20 ticks
                    if (temp > 20) {
                        temp = Math.max(20, temp - INVENTORY_COOL_PER_TICK);
                        TemperatureUtils.setTemperature(stack, temp);
                    }
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
            int prevTemp = temp;
            if (tickCounter % 20 == 0) {
                if (temp > 20) {
                    temp = Math.max(20, temp - INVENTORY_COOL_PER_TICK);
                    TemperatureUtils.setTemperature(stack, temp);
                }
            }
            BlockPos pos = itemEntity.getBlockPos();
            var blockState = world.getBlockState(pos);
            boolean changed = false;
            boolean isWaterCauldron = blockState.isOf(net.minecraft.block.Blocks.WATER_CAULDRON);
            int cauldronLevel = isWaterCauldron && blockState.contains(Properties.LEVEL_3) ? blockState.get(Properties.LEVEL_3) : 0;
            boolean inFilledCauldron = isWaterCauldron && cauldronLevel == 3;
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
        }
    }
}
