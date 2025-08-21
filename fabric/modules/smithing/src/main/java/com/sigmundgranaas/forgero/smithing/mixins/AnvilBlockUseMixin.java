package com.sigmundgranaas.forgero.smithing.mixins;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(net.minecraft.block.AnvilBlock.class)
public abstract class AnvilBlockUseMixin {
	@Unique
	private static final Logger LOGGER = LoggerFactory.getLogger(AnvilBlockUseMixin.class);

	@Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
	private void forgero$customOnUse(
			BlockState state,
			World world,
			BlockPos pos,
			PlayerEntity player,
			Hand hand,
			BlockHitResult hit,
			CallbackInfoReturnable<ActionResult> cir
	) {
		if (world.getBlockEntity(pos) instanceof com.sigmundgranaas.forgero.smithing.block.entity.custom.SmithingAnvilBlockEntity smithingAnvilBlockEntity) {
			if (!smithingAnvilBlockEntity.getInventory().getStack(0).isEmpty()) {
				// Block vanilla UI if item is present
				cir.setReturnValue(ActionResult.SUCCESS);
			}
		}
	}
}
