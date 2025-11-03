package com.sigmundgranaas.forgero.smithing.temperature;

import com.sigmundgranaas.forgero.smithing.networking.S2C.TemperatureSyncS2CPacket;
import com.sigmundgranaas.forgero.smithing.temperature.DynamicTemperatureSystem.TemperatureStage;
import com.sigmundgranaas.forgero.smithing.temperature.DynamicTemperatureSystem.TemperatureStages;

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

            TemperatureStages stages = DynamicTemperatureSystem.calculateStages(stack);
            emitTemperatureEffects(world, itemEntity, pos, temp, stages);

            if (inFilledCauldron) {
                if (temp > 100) {
                    world.spawnParticles(ParticleTypes.CLOUD, itemEntity.getX(), itemEntity.getY() + 0.2, itemEntity.getZ(), 8, 0.2, 0.1, 0.2, 0.01);
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

    private static void emitTemperatureEffects(ServerWorld world, ItemEntity itemEntity, BlockPos pos, int temp, TemperatureStages stages) {
        double x = itemEntity.getX();
        double y = itemEntity.getY() + 0.3;
        double z = itemEntity.getZ();
        int entityId = itemEntity.getId();
        long randomSeed = (long) entityId * 31 + world.getTime();

        TemperatureStage stage = DynamicTemperatureSystem.getStage(temp, stages);

        if (stage == TemperatureStage.COLD) {
            // No effects for cold
        } else if (stage == TemperatureStage.WARM) {
            // Smoke for gentle warmth
            if ((tickCounter + randomSeed) % 2 == 0) {
                world.spawnParticles(ParticleTypes.SMOKE,
                    x, y, z, 2, 0.03, 0.06, 0.03, 0.001);
            }
        } else if (stage == TemperatureStage.HOT) {
            // Small flames for hot
            if ((tickCounter + randomSeed * 2) % 3 == 0) {
                world.spawnParticles(ParticleTypes.SMALL_FLAME,
                    x, y + 0.04, z, 4, 0.025, 0.025, 0.025, 0.003);
            }
            if ((tickCounter + randomSeed * 3) % 3 == 0) {
                world.playSound(null, pos, net.minecraft.sound.SoundEvents.BLOCK_FIRE_AMBIENT,
                    net.minecraft.sound.SoundCategory.BLOCKS, 0.5F, 0.8F + (float)Math.random() * 0.4F);
            }
        } else if (stage == TemperatureStage.WORKABLE) {
            // Bright flames for workable range
            if ((tickCounter + randomSeed * 2) % 2 == 0) {
                world.spawnParticles(ParticleTypes.FLAME,
                    x, y, z, 6, 0.03, 0.04, 0.03, 0.003);
            }
            if ((tickCounter + randomSeed * 3) % 4 == 0) {
                world.playSound(null, pos, net.minecraft.sound.SoundEvents.BLOCK_FIRE_AMBIENT,
                    net.minecraft.sound.SoundCategory.BLOCKS, 0.6F, 1.0F + (float)Math.random() * 0.2F);
            }
        } else if (stage == TemperatureStage.OVERHEATED) {
            // Maximum intensity: intense flames and smoke
            if ((tickCounter + randomSeed * 2) % 2 == 0) {
                world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    x, y, z, 8, 0.04, 0.05, 0.04, 0.003);
                world.spawnParticles(ParticleTypes.SMALL_FLAME,
                    x, y + 0.04, z, 6, 0.03, 0.04, 0.03, 0.002);
            }
            if ((tickCounter + randomSeed * 3) % 2 == 0) {
                world.playSound(null, pos, net.minecraft.sound.SoundEvents.BLOCK_FIRE_AMBIENT,
                    net.minecraft.sound.SoundCategory.BLOCKS, 0.8F, 0.9F + (float)Math.random() * 0.3F);
            }
            if ((tickCounter + randomSeed * 4) % 2 == 0) {
                world.spawnParticles(ParticleTypes.FLAME,
                    x, y, z, 18, 0.06, 0.07, 0.06, 0.004);
                world.spawnParticles(ParticleTypes.SMOKE,
                    x, y, z, 4, 0.04, 0.04, 0.04, 0.002);
            }
            if ((tickCounter + randomSeed * 5) % 2 == 0) {
                world.playSound(null, pos, net.minecraft.sound.SoundEvents.BLOCK_LAVA_AMBIENT,
                    net.minecraft.sound.SoundCategory.BLOCKS, 1.2F, 0.6F + (float)Math.random() * 0.3F);
            }
        }
    }
}
