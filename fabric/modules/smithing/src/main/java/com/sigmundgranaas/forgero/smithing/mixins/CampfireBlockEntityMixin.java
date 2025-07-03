package com.sigmundgranaas.forgero.smithing.mixins;

import com.sigmundgranaas.forgero.minecraft.common.item.StateItem;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils;
import com.sigmundgranaas.forgero.smithing.util.ToolPartTypeUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

@Mixin(CampfireBlockEntity.class)
public class CampfireBlockEntityMixin {
    /**
     * On each tick, increase the temperature of tool parts being cooked on the campfire.
     */

    @Inject(method = "litServerTick", at = @At("TAIL"))
    private static void forgero$increaseToolPartTemperature(net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.BlockState state, CampfireBlockEntity campfire, CallbackInfo ci) {
        DefaultedList<ItemStack> items = campfire.getItemsBeingCooked();boolean changed = false;
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            if (stack.getItem() instanceof StateItem stateItem) {
                var type = stateItem.dynamicState(stack).type();
                if (ToolPartTypeUtils.isToolPartHeadOrToolPart(type)) {
                    int temp = TemperatureUtils.getTemperature(stack);
                    int newTemp = Math.min(temp + 10, TemperatureUtils.MAX_TEMPERATURE);
                    if (newTemp != temp) {
                        TemperatureUtils.setTemperature(stack, newTemp);
                        items.set(i, stack.copy()); // Force update for client sync
                        changed = true;
                    }
                }
            }
        }
        if (changed) {
            campfire.markDirty();
            world.updateListeners(pos, state, state, net.minecraft.block.Block.NOTIFY_ALL);
        }
    }

    @Inject(method = "addItem", at = @At("HEAD"), cancellable = true)
    private void forgero$allowToolPartPlacement(@org.jetbrains.annotations.Nullable net.minecraft.entity.Entity user, ItemStack stack, int cookTime, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() instanceof StateItem stateItem) {
            var type = stateItem.dynamicState(stack).type();
            if (ToolPartTypeUtils.isToolPartHeadOrToolPart(type)) {
                CampfireBlockEntity campfire = (CampfireBlockEntity)(Object)this;
                for (int i = 0; i < campfire.getItemsBeingCooked().size(); i++) {
                    ItemStack itemStack = campfire.getItemsBeingCooked().get(i);
                    if (itemStack.isEmpty()) {
                        campfire.getItemsBeingCooked().set(i, stack.split(1));
                        // Set a default cook time for tool parts (e.g., 600 ticks)
                        try {
                            java.lang.reflect.Field cookingTotalTimes = CampfireBlockEntity.class.getDeclaredField("cookingTotalTimes");
                            java.lang.reflect.Field cookingTimes = CampfireBlockEntity.class.getDeclaredField("cookingTimes");
                            cookingTotalTimes.setAccessible(true);
                            cookingTimes.setAccessible(true);
                            int[] totalTimes = (int[]) cookingTotalTimes.get(campfire);
                            int[] times = (int[]) cookingTimes.get(campfire);
                            totalTimes[i] = 600;
                            times[i] = 0;
                        } catch (Exception e) {
                            // Fallback: do nothing
                        }
                        campfire.getWorld().emitGameEvent(net.minecraft.world.event.GameEvent.BLOCK_CHANGE, campfire.getPos(), net.minecraft.world.event.GameEvent.Emitter.of(user, campfire.getCachedState()));
                        campfire.markDirty();
                        campfire.getWorld().updateListeners(campfire.getPos(), campfire.getCachedState(), campfire.getCachedState(), net.minecraft.block.Block.NOTIFY_ALL);
                        cir.setReturnValue(true);
                        return;
                    }
                }
                cir.setReturnValue(false);
            }
        }
    }

    // Allow picking up tool parts from the campfire at any time
    @Inject(method = "spawnItemsBeingCooked", at = @At("HEAD"), cancellable = true)
    private void forgero$allowPickupAnytime(CallbackInfo ci) {
        CampfireBlockEntity campfire = (CampfireBlockEntity)(Object)this;
        DefaultedList<ItemStack> items = campfire.getItemsBeingCooked();
        boolean pickedUp = false;
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            if (!stack.isEmpty()) {
                // Drop the item and clear the slot
                net.minecraft.util.ItemScatterer.spawn(campfire.getWorld(), campfire.getPos().getX(), campfire.getPos().getY(), campfire.getPos().getZ(), stack);
                items.set(i, ItemStack.EMPTY);
                pickedUp = true;
            }
        }
        if (pickedUp) {
            campfire.markDirty();
            campfire.getWorld().updateListeners(campfire.getPos(), campfire.getCachedState(), campfire.getCachedState(), net.minecraft.block.Block.NOTIFY_ALL);
            ci.cancel(); // Prevent vanilla logic from running
        }
    }
}
