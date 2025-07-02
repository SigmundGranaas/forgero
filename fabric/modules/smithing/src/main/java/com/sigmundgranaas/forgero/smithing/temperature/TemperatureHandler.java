package com.sigmundgranaas.forgero.smithing.temperature;

import com.sigmundgranaas.forgero.minecraft.common.item.StateItem;
import com.sigmundgranaas.forgero.smithing.util.ToolPartTypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class TemperatureHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("ForgeroTemperature");
    private static final int HEAT_PER_TICK = 1;
    private static final int COOL_PER_TICK = 2;

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(TemperatureHandler::onWorldTick);
    }

    private static void onWorldTick(ServerWorld world) {
        Box worldBox = new Box(
            world.getWorldBorder().getBoundWest(), world.getBottomY(), world.getWorldBorder().getBoundNorth(),
            world.getWorldBorder().getBoundEast(), world.getTopY(), world.getWorldBorder().getBoundSouth()
        );
        for (ItemEntity entity : world.getEntitiesByClass(ItemEntity.class, worldBox, entity -> true)) {
            ItemStack stack = entity.getStack();
            LOGGER.debug("Checking item entity: {} at {}", stack.getItem().getTranslationKey(), entity.getBlockPos());
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
            BlockPos pos = entity.getBlockPos();
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
                entity.setStack(stack.copy()); // Force sync to client for real-time color update
                LOGGER.info("Temperature of item {} at {} changed from {} to {}", stack.getItem().getTranslationKey(), pos, prevTemp, clampedTemp);
            }
        }
    }
}
