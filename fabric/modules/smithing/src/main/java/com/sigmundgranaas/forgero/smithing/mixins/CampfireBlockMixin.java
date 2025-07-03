package com.sigmundgranaas.forgero.smithing.mixins;

import com.sigmundgranaas.forgero.minecraft.common.item.StateItem;
import com.sigmundgranaas.forgero.smithing.util.ToolPartTypeUtils;
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
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(CampfireBlock.class)
public class CampfireBlockMixin {
    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    private void forgero$allowToolPartOnCampfire(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack stack = player.getStackInHand(hand);

        // If the player is holding an item, try to place it
        if (!stack.isEmpty() && stack.getItem() instanceof StateItem stateItem) {
            var type = stateItem.dynamicState(stack).type();
            if (ToolPartTypeUtils.isToolPartHeadOrToolPart(type)) {
                BlockEntity be = world.getBlockEntity(pos);
                if (be instanceof CampfireBlockEntity campfire) {
                    if (campfire.addItem(player, stack, 600)) {
                        if (!player.isCreative()) {
                            stack.decrement(1);
                        }
                        cir.setReturnValue(ActionResult.success(world.isClient));
                        return;
                    }
                }
            }
        }

        // If the player's hand is empty, pick up the first item from the campfire
        if (stack.isEmpty()) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof CampfireBlockEntity campfire) {
                for (int i = 0; i < campfire.getItemsBeingCooked().size(); i++) {
                    ItemStack cooked = campfire.getItemsBeingCooked().get(i);
                    if (!cooked.isEmpty()) {
                        if (!player.getInventory().insertStack(cooked.copy())) {
                            player.dropItem(cooked.copy(), false);
                        }
                        campfire.getItemsBeingCooked().set(i, ItemStack.EMPTY);
                        campfire.markDirty();
                        world.updateListeners(pos, state, state, 3);
                        cir.setReturnValue(ActionResult.success(world.isClient));
                        return;
                    }
                }
            }
        }
    }
}
