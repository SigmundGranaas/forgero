package com.sigmundgranaas.forgero.smithing.mixins;

import com.sigmundgranaas.forgero.smithing.block.ModBlocks;
import com.sigmundgranaas.forgero.smithing.block.custom.HearthBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
	private void onIronBarUse(BlockState state, World world, BlockPos pos, PlayerEntity player,
							  Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {

		ItemStack itemStack = player.getStackInHand(hand);

		if (itemStack.getItem() == Items.IRON_BARS) {
			if (!world.isClient) {
				// Get the old block entity data before replacing (if you need to preserve anything)
				BlockEntity oldBlockEntity = world.getBlockEntity(pos);

				// Remove the old block entity first
				world.removeBlockEntity(pos);

				// Replace with your custom block
				BlockState newState = ModBlocks.HEARTH.getDefaultState()
						.with(HearthBlock.FACING, state.get(CampfireBlock.FACING))
						.with(HearthBlock.LIT, state.get(CampfireBlock.LIT))
						.with(HearthBlock.SIGNAL_FIRE, state.get(CampfireBlock.SIGNAL_FIRE))
						.with(HearthBlock.WATERLOGGED, state.get(CampfireBlock.WATERLOGGED));

				world.setBlockState(pos, newState, Block.NOTIFY_ALL);

				// Create and set the new block entity
				world.setBlockState(pos, newState, Block.NOTIFY_ALL);

				if (!player.getAbilities().creativeMode) {
					itemStack.decrement(1);
				}

				world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1.0f, 1.0f);
			}

			cir.setReturnValue(ActionResult.SUCCESS);
		}
	}
}
