package com.sigmundgranaas.forgero.smithing.mixins;

import com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(CampfireBlock.class)
public class CampfireBlockMixin {
    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    private void forgero$allowHeatingItems(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack stack = player.getStackInHand(hand);
        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof CampfireBlockEntity campfire)) {
            return;
        }

        if (!state.get(CampfireBlock.LIT) || !stack.isEmpty() && !TemperatureUtils.hasMaxTemperature(stack)) {
            return;
        }

        if (stack.isEmpty()) {
            for (int i = 0; i < campfire.getItemsBeingCooked().size(); i++) {
                ItemStack cooked = campfire.getItemsBeingCooked().get(i);
                if (!cooked.isEmpty() && TemperatureUtils.hasMaxTemperature(cooked)) {
                    if (!player.getInventory().insertStack(cooked.copy())) {
                        player.dropItem(cooked.copy(), false);
                    }
                    campfire.getItemsBeingCooked().set(i, ItemStack.EMPTY);
                    campfire.markDirty();
                    world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5f, 1.0f);
                    cir.setReturnValue(ActionResult.success(world.isClient));
                    return;
                }
            }
            return;
        }

        int veryLongCookTime = Integer.MAX_VALUE;
        if (campfire.addItem(player, stack, veryLongCookTime)) {
            world.playSound(null, pos, SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.BLOCKS, 0.5f, 1.0f);
            cir.setReturnValue(ActionResult.success(world.isClient));
        }
    }
}
