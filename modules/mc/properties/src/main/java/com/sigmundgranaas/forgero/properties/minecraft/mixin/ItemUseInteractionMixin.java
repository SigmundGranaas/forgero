package com.sigmundgranaas.forgero.properties.minecraft.mixin;

import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.UseInteractionManager;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin for {@link Item} class to inject UseInteractionProperty handling.
 * This mixin checks if an ItemStack has a UseInteractionProperty and delegates
 * to the UseInteractionManager for handling the lifecycle.
 *
 * <p>Unlike the DynamicItemUseHandler approach, this mixin works based on the
 * ItemStack's properties rather than the Item class implementation.</p>
 */
@Mixin(Item.class)
public abstract class ItemUseInteractionMixin {

	/**
	 * Injects into {@link Item#use(World, PlayerEntity, Hand)} to handle
	 * the start of use interactions.
	 */
	@Inject(method = "use", at = @At("HEAD"), cancellable = true)
	private void forgero$useInteractionUse(World world, PlayerEntity user, Hand hand,
	                                        CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
		ItemStack stack = user.getStackInHand(hand);

		if (UseInteractionManager.hasUseInteraction(stack)) {
			TypedActionResult<ItemStack> result = UseInteractionManager.handleUse(world, user, hand);

			// Only cancel if we actually handled it (not PASS)
			if (result.getResult() != ActionResult.PASS) {
				cir.setReturnValue(result);
				cir.cancel();
			}
		}
	}

	/**
	 * Injects into {@link Item#getUseAction(ItemStack)} to return the
	 * use action from UseInteractionProperty.
	 */
	@Inject(method = "getUseAction", at = @At("HEAD"), cancellable = true)
	private void forgero$useInteractionGetUseAction(ItemStack stack, CallbackInfoReturnable<UseAction> cir) {
		UseInteractionManager.getUseAction(stack).ifPresent(action -> {
			cir.setReturnValue(action);
			cir.cancel();
		});
	}

	/**
	 * Injects into {@link Item#getMaxUseTime(ItemStack)} to return the
	 * max use time from UseInteractionProperty.
	 */
	@Inject(method = "getMaxUseTime", at = @At("HEAD"), cancellable = true)
	private void forgero$useInteractionGetMaxUseTime(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
		UseInteractionManager.getMaxUseTime(stack).ifPresent(maxTime -> {
			cir.setReturnValue(maxTime);
			cir.cancel();
		});
	}

	/**
	 * Injects into {@link Item#usageTick(World, LivingEntity, ItemStack, int)} to handle
	 * tick effects during use.
	 */
	@Inject(method = "usageTick", at = @At("HEAD"))
	private void forgero$useInteractionUsageTick(World world, LivingEntity user, ItemStack stack,
	                                              int remainingUseTicks, CallbackInfo ci) {
		if (UseInteractionManager.hasUseInteraction(stack)) {
			UseInteractionManager.handleTick(world, user, stack, remainingUseTicks);
		}
	}

	/**
	 * Injects into {@link Item#onStoppedUsing(ItemStack, World, LivingEntity, int)} to handle
	 * release effects.
	 */
	@Inject(method = "onStoppedUsing", at = @At("HEAD"))
	private void forgero$useInteractionOnStoppedUsing(ItemStack stack, World world, LivingEntity user,
	                                                   int remainingUseTicks, CallbackInfo ci) {
		if (UseInteractionManager.hasUseInteraction(stack)) {
			UseInteractionManager.handleRelease(stack, world, user, remainingUseTicks);
		}
	}

	/**
	 * Injects into {@link Item#finishUsing(ItemStack, World, LivingEntity)} to handle
	 * finish effects.
	 */
	@Inject(method = "finishUsing", at = @At("HEAD"), cancellable = true)
	private void forgero$useInteractionFinishUsing(ItemStack stack, World world, LivingEntity user,
	                                                CallbackInfoReturnable<ItemStack> cir) {
		if (UseInteractionManager.hasUseInteraction(stack)) {
			ItemStack result = UseInteractionManager.handleFinish(stack, world, user);
			cir.setReturnValue(result);
			cir.cancel();
		}
	}

	/**
	 * Injects into {@link Item#isUsedOnRelease(ItemStack)} to check if the item
	 * fires its action on release vs finish.
	 */
	@Inject(method = "isUsedOnRelease", at = @At("HEAD"), cancellable = true)
	private void forgero$useInteractionIsUsedOnRelease(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		UseInteractionManager.isUsedOnRelease(stack).ifPresent(usedOnRelease -> {
			cir.setReturnValue(usedOnRelease);
			cir.cancel();
		});
	}
}
