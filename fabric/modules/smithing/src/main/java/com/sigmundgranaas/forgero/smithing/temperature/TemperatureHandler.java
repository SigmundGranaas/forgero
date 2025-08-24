package com.sigmundgranaas.forgero.smithing.temperature;

import com.sigmundgranaas.forgero.minecraft.common.item.StateItem;
import com.sigmundgranaas.forgero.smithing.util.TemperatureItemUtil;
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
        // Removed END_PLAYER_TICK, not available in Fabric API
    }

    private static void onWorldTick(ServerWorld world) {
        tickCounter++;
        if (tickCounter % TICK_INTERVAL != 0) {
            return;
        }
        // Cool down items in player inventories
        for (ServerPlayerEntity player : world.getPlayers()) {
            boolean tookHeatDamage = false;
            for (int i = 0; i < player.getInventory().size(); i++) {
                ItemStack stack = player.getInventory().getStack(i);
                if (!(stack.getItem() instanceof StateItem stateItem)) {
                    continue;
                }
                var type = stateItem.dynamicState(stack).type();
                if (!TemperatureUtils.hasMaxTemperature(stack)) {
                    continue;
                }
                int temp = TemperatureUtils.getTemperature(stack);
                int prevTemp = temp;
                // Damage player if holding or carrying hot item
                if (temp > 100) {
                    // Check if in hand
                    if (player.getMainHandStack() == stack || player.getOffHandStack() == stack) {
                        tookHeatDamage = true;
                    } else {
                        // Also damage if anywhere in inventory
                        tookHeatDamage = true;
                    }
                }
                // Log every time the inventory cooling logic is checked
                if (tickCounter % 20 == 0) { // Changed to every 20 ticks
                    if (temp > 20) {
                        temp = Math.max(20, temp - INVENTORY_COOL_PER_TICK);
                        TemperatureUtils.setTemperature(stack, temp);
                    }
                    LOGGER.debug("Inventory cooling checked for {}: {} -> {}", stack.getName().getString(), prevTemp, temp);
                }
            }
            if (tookHeatDamage) {
                player.damage(world.getDamageSources().hotFloor(), 1.0F);
            }
        }
        for (var entity : world.iterateEntities()) {
            if (!(entity instanceof ItemEntity itemEntity)) continue;
            ItemStack stack = itemEntity.getStack();
            LOGGER.debug("Checking item entity: {} at {}", stack.getItem().getTranslationKey(), itemEntity.getBlockPos());
            // Only apply to tool part heads or tool parts
            if (!(stack.getItem() instanceof StateItem stateItem)) {
                LOGGER.debug("Skipped: Not a StateItem");
                continue;
            }
            var type = stateItem.dynamicState(stack).type();
            LOGGER.debug("Type for item {}: {}", stack.getItem().getTranslationKey(), type.typeName());
            if (!TemperatureUtils.hasMaxTemperature(stack)) {
                LOGGER.debug("Skipped: Not a tool part head or tool part");
                continue;
            }
            BlockPos pos = itemEntity.getBlockPos();
            var blockState = world.getBlockState(pos);
            // Use the block at the item's position for cauldron detection
            LOGGER.info("[Forgero] Item at {}: blockAt registry={} class={}", pos, blockState.getBlock().getTranslationKey(), blockState.getBlock().getClass().getName());
            boolean changed = false;
            int temp = TemperatureUtils.getTemperature(stack);
            int prevTemp = temp;
            // Improved cauldron detection for water cauldron at the item's position
            boolean isWaterCauldron = blockState.isOf(net.minecraft.block.Blocks.WATER_CAULDRON);
            int cauldronLevel = isWaterCauldron && blockState.contains(Properties.LEVEL_3) ? blockState.get(Properties.LEVEL_3) : 0;
            boolean inFilledCauldron = isWaterCauldron && cauldronLevel == 3;
            LOGGER.info("[Forgero] Checking for filled water cauldron at item pos {}: isWaterCauldron={} level={}", pos, isWaterCauldron, cauldronLevel);
            if (inFilledCauldron) {
                if (temp > 100) {
                    // Spawn cloud particles and play extinguish sound
                    world.spawnParticles(net.minecraft.particle.ParticleTypes.CLOUD, itemEntity.getX(), itemEntity.getY() + 0.2, itemEntity.getZ(), 8, 0.2, 0.1, 0.2, 0.01);
                    world.playSound(null, pos, net.minecraft.sound.SoundEvents.BLOCK_FIRE_EXTINGUISH, net.minecraft.sound.SoundCategory.BLOCKS, 0.7F, 1.2F);
                }
                // --- Fix: Always cool if temp > 20, not just once ---
                if (temp > 20) {
                    temp = Math.max(20, temp - FLUID_COOL_PER_TICK);
                    TemperatureUtils.setTemperature(stack, temp);
                    changed = true;
                    LOGGER.info("[Forgero] Cooling down item at {}: {} -> {} (filled water cauldron)", pos, prevTemp, temp);
                }
            } else {
                LOGGER.info("[Forgero] No heating/cooling at {}: block={} (no effect)", pos, blockState.getBlock().getTranslationKey());
            }
        }
    }
}
