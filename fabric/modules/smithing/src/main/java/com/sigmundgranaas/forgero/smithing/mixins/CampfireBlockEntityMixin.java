package com.sigmundgranaas.forgero.smithing.mixins;

import com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(CampfireBlockEntity.class)
public abstract class CampfireBlockEntityMixin {
    @Shadow @Final private DefaultedList<ItemStack> itemsBeingCooked;
    @Shadow @Final private int[] cookingTimes;
    @Shadow @Final private int[] cookingTotalTimes;

    @Unique private static final int FORGERO_HEAT_PER_TICK = 2; // Adjust as desired

    @Inject(method = "litServerTick", at = @At("HEAD"))
    private static void forgero$heatUp(World world, BlockPos pos, BlockState state, CampfireBlockEntity campfire, CallbackInfo ci) {
        CampfireBlockEntityMixin accessor = (CampfireBlockEntityMixin)(Object)campfire;
        for (int i = 0; i < accessor.itemsBeingCooked.size(); i++) {
            ItemStack stack = accessor.itemsBeingCooked.get(i);
            if (stack.isEmpty() || !TemperatureUtils.hasMaxTemperature(stack)) {
                continue;
            }

            accessor.cookingTotalTimes[i] = Integer.MAX_VALUE;

            int max = TemperatureUtils.getMaxTemp(stack);
            if (max <= 0) {
                continue;
            }

            int current = TemperatureUtils.getTemperature(stack);
            int next = Math.min(max, current + FORGERO_HEAT_PER_TICK);
            if (next != current) {
                TemperatureUtils.setTemperature(stack, next);
                campfire.markDirty();
                world.updateListeners(pos, state, state, 3);
            }
        }
    }
}

