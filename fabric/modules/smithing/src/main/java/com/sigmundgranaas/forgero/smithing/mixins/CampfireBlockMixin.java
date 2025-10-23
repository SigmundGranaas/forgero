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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(CampfireBlock.class)
public class CampfireBlockMixin {
    private static final int INGOT_SLOT = 0;
    // Common tag names used by many mods/datapacks. Adjust if your project uses different namespaces.
    private static final TagKey<Item> TAG_C_INGOTS = TagKey.of(RegistryKeys.ITEM, new Identifier("c", "ingots"));
    private static final TagKey<Item> TAG_FORGE_INGOTS = TagKey.of(RegistryKeys.ITEM, new Identifier("forge", "ingots"));

    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    private void forgero$allowHeatingItems(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack stack = player.getStackInHand(hand);
        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof CampfireBlockEntity campfire)) {
            return;
        }

        if (!state.get(CampfireBlock.LIT)) {
            return;
        }

        // Block other items when an ingot occupies the special slot
        if (!stack.isEmpty()) {
            ItemStack ingotSlot = campfire.getItemsBeingCooked().get(INGOT_SLOT);
            if (!ingotSlot.isEmpty() && !isIngot(stack)) {
                // Consume the interaction without placing
                cir.setReturnValue(ActionResult.SUCCESS);
                return;
            }
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

        // Put ingots into the dedicated slot 0
        if (isIngot(stack)) {
            ItemStack slotStack = campfire.getItemsBeingCooked().get(INGOT_SLOT);
            if (slotStack.isEmpty()) {
                ItemStack toPlace = stack.copy();
                toPlace.setCount(1);
                campfire.getItemsBeingCooked().set(INGOT_SLOT, toPlace);
                stack.decrement(1);
                campfire.markDirty();
                // Ensure client sees the slot update immediately
                world.updateListeners(pos, state, state, 3);
                world.playSound(null, pos, SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.BLOCKS, 0.5f, 1.0f);
                cir.setReturnValue(ActionResult.success(world.isClient));
                return;
            }
            // If special slot is taken, fall through to vanilla handling (do not cancel)
        }
    }

    private static boolean isIngot(ItemStack stack) {
        return stack.isIn(TAG_C_INGOTS) || stack.isIn(TAG_FORGE_INGOTS) || stack.getItem().toString().endsWith("_ingot");
    }
}
