package com.sigmundgranaas.forgero.smithing.mixins;

import com.sigmundgranaas.forgero.smithing.block.entity.custom.SmithingAnvilBlockEntity;
import com.sigmundgranaas.forgero.smithing.item.ModItemTags;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(net.minecraft.block.AnvilBlock.class)
public abstract class AnvilBlockUseMixin {
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
		if (!(world.getBlockEntity(pos) instanceof SmithingAnvilBlockEntity smithingAnvil)) {
			return;
		}

		if (smithingAnvil.isGuiBlocked(world)) {
			cir.setReturnValue(ActionResult.SUCCESS);
			return;
		}

		if (smithingAnvil.getInventory().getStack(0).isEmpty()) {
			return;
		}

		ItemStack stackInHand = player.getStackInHand(hand);

		if (!stackInHand.isIn(ModItemTags.SMITHING_HAMMERS)) {
			cir.setReturnValue(ActionResult.SUCCESS);
			return;
		}

		cir.setReturnValue(ActionResult.SUCCESS);

		if (!world.isClient) {
			cir.setReturnValue(smithingAnvil.onHammerHit(player, hit));
		}
	}
}
