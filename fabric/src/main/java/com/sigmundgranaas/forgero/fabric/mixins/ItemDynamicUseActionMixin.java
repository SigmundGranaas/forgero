package com.sigmundgranaas.forgero.fabric.mixins;

import com.sigmundgranaas.forgero.feature.OnUseFeature;
import com.sigmundgranaas.forgero.handler.afterUse.AfterUseHandler;
import com.sigmundgranaas.forgero.handler.use.BlockUseHandler;
import com.sigmundgranaas.forgero.handler.use.EntityUseHandler;
import com.sigmundgranaas.forgero.handler.use.StopHandler;
import com.sigmundgranaas.forgero.handler.use.UseHandler;
import com.sigmundgranaas.forgero.item.DynamicItemUseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

/**
 * Mixin for {@link Item} class to inject custom behavior into all use-action-related methods.
 * This mixin allows items to dynamically alter their use actions and other related behaviors at runtime,
 * in conjunction with the {@link OnUseFeature} class.
 * <p>
 * This mixin is injected into the {@link Item} class, so you won't have to override every single method in you custom classes.
 * Implement the {@link DynamicItemUseHandler} interface in your custom item class to enable dynamic use actions.
 *
 * <p>The {@link OnUseFeature} implements interfaces like {@link BlockUseHandler}, {@link EntityUseHandler},
 * {@link UseHandler}, {@link AfterUseHandler}, and {@link StopHandler} to handle different types of item interactions.</p>
 */
@Mixin(Item.class)
public abstract class ItemDynamicUseActionMixin {

	/**
	 * Injects custom logic into {@link Item#getUseAction(ItemStack)}.
	 * Delegates the determination of the use action to the {@link OnUseFeature}.
	 *
	 * @param stack The item stack being checked.
	 * @param cir   The callback info returnable for the use action.
	 */
	@Inject(method = "getUseAction", at = @At("HEAD"), cancellable = true)
	private void forgero$DynamicGetUseAction(ItemStack stack, CallbackInfoReturnable<UseAction> cir) {
		if (this instanceof DynamicItemUseHandler dynamicUseHandler) {
			cir.setReturnValue(dynamicUseHandler.dynamicGetUseAction(stack));
			cir.cancel();
		}
	}

	/**
	 * Injects custom logic into {@link Item#getMaxUseTime(ItemStack)}.
	 * Delegates the determination of the maximum use time to the {@link OnUseFeature}.
	 *
	 * @param stack The item stack being checked.
	 * @param cir   The callback info returnable for the maximum use time.
	 */
	@Inject(method = "getMaxUseTime", at = @At("HEAD"), cancellable = true)
	private void forgero$DynamicGetMaxUseTime(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
		if (this instanceof DynamicItemUseHandler dynamicUseHandler) {
			cir.setReturnValue(dynamicUseHandler.dynamicGetMaxUseTime(stack));
			cir.cancel();
		}
	}

	/**
	 * Injects custom logic into {@link Item#usageTick(World, LivingEntity, ItemStack, int)}.
	 * Delegates the tick usage functionality to the {@link OnUseFeature}.
	 *
	 * @param world             The world in which the item is used.
	 * @param user              The entity using the item.
	 * @param stack             The item stack being used.
	 * @param remainingUseTicks The number of ticks remaining for use.
	 * @param ci                The callback info.
	 */
	@Inject(method = "usageTick", at = @At("HEAD"))
	private void forgero$DynamicUsageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks, CallbackInfo ci) {
		if (this instanceof DynamicItemUseHandler dynamicUseHandler) {
			dynamicUseHandler.dynamicUsageTick(world, user, stack, remainingUseTicks);
		}
	}

	/**
	 * Injects custom logic into {@link Item#finishUsing(ItemStack, World, LivingEntity)}.
	 * Delegates the finish using action to the {@link OnUseFeature}.
	 *
	 * @param stack The item stack being finished.
	 * @param world The world in which the item is used.
	 * @param user  The entity finishing the use of the item.
	 * @param cir   The callback info returnable for the resulting item stack.
	 */
	@Inject(method = "finishUsing", at = @At("HEAD"), cancellable = true)
	private void forgero$DynamicFinishUsing(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
		if (this instanceof DynamicItemUseHandler dynamicUseHandler) {
			cir.setReturnValue(dynamicUseHandler.dynamicFinishUsing(stack, world, user));
			cir.cancel();
		}
	}

	/**
	 * Injects custom logic into {@link Item#onStoppedUsing(ItemStack, World, LivingEntity, int)}.
	 * Delegates the stop using action to the {@link OnUseFeature}.
	 *
	 * @param stack             The item stack that was being used.
	 * @param world             The world in which the item was used.
	 * @param user              The entity that stopped using the item.
	 * @param remainingUseTicks The number of ticks left when the item was stopped being used.
	 * @param ci                The callback info.
	 */
	@Inject(method = "onStoppedUsing", at = @At("HEAD"))
	private void forgero$DynamicOnStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
		if (this instanceof DynamicItemUseHandler dynamicUseHandler) {
			dynamicUseHandler.dynamicOnStoppedUsing(stack, world, user, remainingUseTicks);
		}
	}


	/**
	 * Injects custom logic into {@link Item#isUsedOnRelease(ItemStack)}.
	 * Determines if the item is used on release based on the {@link OnUseFeature}.
	 *
	 * @param stack The item stack being checked.
	 * @param cir   The callback info returnable for the boolean result.
	 */
	@Inject(method = "isUsedOnRelease", at = @At("HEAD"), cancellable = true)
	private void forgero$DynamicIsUsedOnRelease(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (this instanceof DynamicItemUseHandler dynamicUseHandler) {
			cir.setReturnValue(dynamicUseHandler.dynamicIsUsedOnRelease(stack));
			cir.cancel();
		}
	}

	/**
	 * Injects custom logic into {@link Item#useOnBlock(ItemUsageContext)}.
	 * Delegates the action when an item is used on a block to the {@link OnUseFeature}.
	 * This allows for dynamic handling of item-block interactions based on the state and behavior
	 * defined in the implementing class of {@link DynamicItemUseHandler}.
	 *
	 * @param context The context in which the item is used on a block.
	 * @param cir     The callback info returnable for the action result.
	 */
	@Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
	private void forgero$DynamicUseOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
		if (this instanceof DynamicItemUseHandler dynamicUseHandler) {
			cir.setReturnValue(dynamicUseHandler.dynamicUseOnBlock(context));
			cir.cancel();
		}
	}

	/**
	 * Injects custom logic into {@link Item#useOnEntity(ItemStack, PlayerEntity, LivingEntity, Hand)}.
	 * Delegates the action when an item is used on an entity to the {@link OnUseFeature}.
	 *
	 * @param stack  The item stack being used.
	 * @param user   The player using the item.
	 * @param entity The entity on which the item is used.
	 * @param hand   The hand in which the item is held.
	 * @param cir    The callback info returnable for the action result.
	 */
	@Inject(method = "useOnEntity", at = @At("HEAD"), cancellable = true)
	private void forgero$DynamicUseOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
		if (this instanceof DynamicItemUseHandler dynamicUseHandler) {
			cir.setReturnValue(dynamicUseHandler.dynamicUseOnEntity(stack, user, entity, hand));
			cir.cancel();
		}
	}

	/**
	 * Injects custom logic into {@link Item#use(World, PlayerEntity, Hand)}.
	 * Delegates the general use action of the item to the {@link OnUseFeature}.
	 *
	 * @param world The world in which the item is used.
	 * @param user  The player using the item.
	 * @param hand  The hand holding the item.
	 * @param cir   The callback info returnable for the typed action result.
	 */
	@Inject(method = "use", at = @At("HEAD"), cancellable = true)
	private void forgero$DynamicUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
		if (this instanceof DynamicItemUseHandler dynamicUseHandler) {
			cir.setReturnValue(dynamicUseHandler.dynamicUse(world, user, hand));
			cir.cancel();
		}
	}
}
