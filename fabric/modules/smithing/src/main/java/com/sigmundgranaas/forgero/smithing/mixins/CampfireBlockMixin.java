package com.sigmundgranaas.forgero.smithing.mixins;

import com.sigmundgranaas.forgero.smithing.campfire.ExtraHeatSlotProvider;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(CampfireBlock.class)
public class CampfireBlockMixin {
    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    private void forgero$extraSlotInteraction(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof CampfireBlockEntity campfire)) {
            return;
        }
        ExtraHeatSlotProvider provider = (ExtraHeatSlotProvider)(Object)campfire;
        ItemStack handStack = player.getStackInHand(hand);

        // If extra slot is occupied, block all vanilla slot interactions
        if (provider.forgero$hasExtraHeatItem()) {
            // Empty hand -> retrieve from extra slot only
            if (handStack.isEmpty()) {
                if (!world.isClient) {
                    ItemStack extra = provider.forgero$getExtraHeatSlot();
                    if (!extra.isEmpty()) {
                        ItemStack toGive = extra.copy();
                        provider.forgero$setExtraHeatSlot(ItemStack.EMPTY);
                        if (!player.getInventory().insertStack(toGive)) {
                            player.dropItem(toGive, false);
                        }
                        campfire.markDirty();
                        world.updateListeners(pos, state, state, 3);
                        if (world instanceof ServerWorld server) server.getChunkManager().markForUpdate(pos);
                        world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5f, 1.0f);
                        cir.setReturnValue(ActionResult.SUCCESS);
                        return;
                    }
                } else {
                    cir.setReturnValue(ActionResult.SUCCESS);
                    return;
                }
            }
            // Non-empty hand -> block placement into vanilla slots
            cir.setReturnValue(ActionResult.PASS);
            return;
        }

        // Empty hand and extra slot is empty -> vanilla behavior
        if (handStack.isEmpty()) {
            return;
        }

        // Place into extra slot if eligible and empty
        if (TemperatureUtils.hasMaxTemperature(handStack)) {
            if (!world.isClient) {
                ItemStack toPlace = handStack.copy();
                toPlace.setCount(1);
                provider.forgero$setExtraHeatSlot(toPlace);
                handStack.decrement(1);
                campfire.markDirty();
                world.updateListeners(pos, state, state, 3);
                if (world instanceof ServerWorld server) server.getChunkManager().markForUpdate(pos);
                world.playSound(null, pos, SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.BLOCKS, 0.5f, 1.0f);
                cir.setReturnValue(ActionResult.SUCCESS);
                return;
            } else {
                cir.setReturnValue(ActionResult.SUCCESS);
                return;
            }
        }
        // Fall through to vanilla behavior
    }

    @Inject(method = "onStateReplaced", at = @At("HEAD"))
    private void forgero$dropExtraOnReplace(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved, CallbackInfo ci) {
        if (world.isClient) return;
        if (state.isOf(newState.getBlock())) return; // only when block actually changed
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof CampfireBlockEntity campfire) {
            ExtraHeatSlotProvider provider = (ExtraHeatSlotProvider)(Object)campfire;
            ItemStack extra = provider.forgero$getExtraHeatSlot();
            if (extra != null && !extra.isEmpty()) {
                ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), extra);
                provider.forgero$setExtraHeatSlot(ItemStack.EMPTY);
            }
        }
    }
}
