package com.sigmundgranaas.forgero.smithing.mixins;

import com.sigmundgranaas.forgero.minecraft.common.item.StateItem;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureColorProvider;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils;
import com.sigmundgranaas.forgero.smithing.util.ToolPartTypeUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.collection.DefaultedList;

@Mixin(CampfireBlockEntity.class)
public class CampfireBlockEntityMixin {
    // Track last temperature per slot to only trigger effects on crossing 100s
    private static final int CAMPFIRE_SLOTS = 4;
    private static final int[] lastToolPartTemps = new int[CAMPFIRE_SLOTS];
    // Track last orange state per slot to only trigger orange sound once per crossing
    private static final boolean[] lastOrangeState = new boolean[CAMPFIRE_SLOTS];

    /**
     * On each tick, increase the temperature of tool parts being cooked on the campfire.
     */

    @Inject(method = "litServerTick", at = @At("TAIL"))
    private static void forgero$increaseToolPartTemperature(net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.BlockState state, CampfireBlockEntity campfire, CallbackInfo ci) {
        DefaultedList<ItemStack> items = campfire.getItemsBeingCooked();
        boolean changed = false;
        boolean isSoulCampfire = state.getBlock() == net.minecraft.block.Blocks.SOUL_CAMPFIRE;
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            if (stack.getItem() instanceof StateItem stateItem) {
                var type = stateItem.dynamicState(stack).type();
                if (ToolPartTypeUtils.isToolPartType(type)) {
                    int temp = TemperatureUtils.getTemperature(stack);
                    int newTemp = Math.min(temp + 1, TemperatureUtils.getMaxTemp(stack)); // Reduced from +10 to +2
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

    @Inject(method = "litServerTick", at = @At("HEAD"), cancellable = true)
    private static void forgero$preventEjectAtMaxTemp(net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.BlockState state, CampfireBlockEntity campfire, CallbackInfo ci) {
        DefaultedList<ItemStack> items = campfire.getItemsBeingCooked();
        try {
            java.lang.reflect.Field cookingTimes = CampfireBlockEntity.class.getDeclaredField("cookingTimes");
            java.lang.reflect.Field cookingTotalTimes = CampfireBlockEntity.class.getDeclaredField("cookingTotalTimes");
            cookingTimes.setAccessible(true);
            cookingTotalTimes.setAccessible(true);
            int[] times = (int[]) cookingTimes.get(campfire);
            int[] totalTimes = (int[]) cookingTotalTimes.get(campfire);
            for (int i = 0; i < items.size(); i++) {
                ItemStack stack = items.get(i);
                if (stack.getItem() instanceof StateItem stateItem) {
                    var type = stateItem.dynamicState(stack).type();
                    if (ToolPartTypeUtils.isToolPartType(type)) {
                        // Always reset cook time for tool parts so they never pop off
                        times[i] = 0;
                        totalTimes[i] = 600; // Or any value, it's ignored
                    }
                }
            }
        } catch (Exception e) {
            // Log or ignore
        }
    }

    @Inject(method = "addItem", at = @At("HEAD"), cancellable = true)
    private void forgero$allowToolPartPlacement(@org.jetbrains.annotations.Nullable net.minecraft.entity.Entity user, ItemStack stack, int cookTime, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() instanceof StateItem stateItem) {
            var type = stateItem.dynamicState(stack).type();
            if (ToolPartTypeUtils.isToolPartType(type)) {
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

    @Inject(method = "litServerTick", at = @At("TAIL"))
    private static void forgero$toolPartCampfireEffects(net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.BlockState state, CampfireBlockEntity campfire, CallbackInfo ci) {
        DefaultedList<ItemStack> items = campfire.getItemsBeingCooked();
        boolean isLitCampfire = state.isOf(net.minecraft.block.Blocks.CAMPFIRE) || state.isOf(net.minecraft.block.Blocks.SOUL_CAMPFIRE);
        if (!isLitCampfire || !state.get(net.minecraft.state.property.Properties.LIT)) return;
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            if (stack.getItem() instanceof StateItem stateItem) {
                var type = stateItem.dynamicState(stack).type();
                if (ToolPartTypeUtils.isToolPartType(type)) {
                    int temp = TemperatureUtils.getTemperature(stack);
                    int lastTemp = lastToolPartTemps[i];
                    // Only play effects when crossing a new 100s boundary
                    if (temp > 0 && (temp / 100) > (lastTemp / 100)) {
                        world.playSound(null, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, net.minecraft.sound.SoundEvents.BLOCK_FIRE_AMBIENT, net.minecraft.sound.SoundCategory.BLOCKS, 0.7F, 1.0F);
                        world.addParticle(net.minecraft.particle.ParticleTypes.LAVA, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 0, 0.05, 0);
                    }
                    // Play a special sound when reaching the orange stage
                    boolean isNowOrange = TemperatureColorProvider.inFirstStageSmithing(temp, TemperatureUtils.getMaxTemp(stack));
                    if (isNowOrange && !lastOrangeState[i]) {
                        world.playSound(null, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, net.minecraft.sound.SoundCategory.BLOCKS, 5.0F, 1.2F);
                    }
                    lastOrangeState[i] = isNowOrange;
                    lastToolPartTemps[i] = temp;
                } else {
                    lastToolPartTemps[i] = 0;
                    lastOrangeState[i] = false;
                }
            } else {
                lastToolPartTemps[i] = 0;
                lastOrangeState[i] = false;
            }
        }
    }
}
