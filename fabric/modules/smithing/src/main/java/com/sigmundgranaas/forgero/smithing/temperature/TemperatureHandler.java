package com.sigmundgranaas.forgero.smithing.temperature;

import com.sigmundgranaas.forgero.minecraft.common.item.StateItem;
import com.sigmundgranaas.forgero.smithing.util.ToolPartTypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class TemperatureHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("ForgeroTemperature");
    private static final int HEAT_PER_TICK = 1;
    private static final int COOL_PER_TICK = 2;
    private static final int INVENTORY_COOL_PER_TICK = 1; // Slower cooling in inventory
    private static int tickCounter = 0;
    private static final int TICK_INTERVAL = 5; // Only update every 5 ticks

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
                if (!ToolPartTypeUtils.isToolPartHeadOrToolPart(type)) {
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
                LOGGER.debug("Inventory cooling check: {} temp={} prevTemp={}", stack.getItem().getTranslationKey(), temp, prevTemp);
                // Cool in inventory down to DEFAULT_TEMPERATURE, slower than in fluid
                if (temp > TemperatureUtils.DEFAULT_TEMPERATURE) {
                    temp -= INVENTORY_COOL_PER_TICK;
                    if (temp < TemperatureUtils.DEFAULT_TEMPERATURE) temp = TemperatureUtils.DEFAULT_TEMPERATURE;
                    if (temp != prevTemp) {
                        TemperatureUtils.setTemperature(stack, temp);
                        LOGGER.debug("Cooled inventory item {} to {} (inventory slow)", stack.getItem().getTranslationKey(), temp);
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
            LOGGER.debug("Checking item entity: {} at {}", stack.getItem().getTranslationKey(), itemEntity.getBlockPos());
            // Only apply to tool part heads or tool parts
            if (!(stack.getItem() instanceof StateItem stateItem)) {
                LOGGER.debug("Skipped: Not a StateItem");
                continue;
            }
            var type = stateItem.dynamicState(stack).type();
            LOGGER.debug("Type for item {}: {}", stack.getItem().getTranslationKey(), type.typeName());
            if (!ToolPartTypeUtils.isToolPartHeadOrToolPart(type)) {
                LOGGER.debug("Skipped: Not a tool part head or tool part");
                continue;
            }
            BlockPos pos = itemEntity.getBlockPos();
            var blockState = world.getBlockState(pos);
            var blockStateBelow = world.getBlockState(pos.down());
            LOGGER.debug("Block at {}: {} | Block below: {}", pos, blockState.getBlock().getTranslationKey(), blockStateBelow.getBlock().getTranslationKey());
            boolean changed = false;
            int temp = TemperatureUtils.getTemperature(stack);
            int prevTemp = temp;
            boolean inMagma = blockState.isOf(Blocks.MAGMA_BLOCK) || blockStateBelow.isOf(Blocks.MAGMA_BLOCK);
            boolean inFluid = !blockState.getFluidState().isEmpty();
            // Heat up if on magma block (at or below)
            if (inMagma) {
                temp += HEAT_PER_TICK;
                changed = true;
                LOGGER.debug("Heating up item at {}: {} -> {} (magma)", pos, prevTemp, temp);
            // Cool down if in water or any liquid
            } else if (inFluid) {
                temp -= COOL_PER_TICK;
                changed = true;
                LOGGER.debug("Cooling down item at {}: {} -> {} (fluid)", pos, prevTemp, temp);
            } else {
                LOGGER.debug("No heating/cooling at {}: block={} blockBelow={} fluid={} (no effect)", pos, blockState.getBlock().getTranslationKey(), blockStateBelow.getBlock().getTranslationKey(), blockState.getFluidState().getFluid().toString());
            }
            // Clamp temperature
            int clampedTemp = TemperatureUtils.clamp(temp);
            if (clampedTemp != temp) {
                LOGGER.debug("Clamped temperature at {}: {} -> {}", pos, temp, clampedTemp);
            }
            if (changed) {
                TemperatureUtils.setTemperature(stack, clampedTemp);
                itemEntity.setStack(stack.copy()); // Force sync to client for real-time color update
                LOGGER.info("Temperature of item {} at {} changed from {} to {}", stack.getItem().getTranslationKey(), pos, prevTemp, clampedTemp);
            }
        }
    }
}
