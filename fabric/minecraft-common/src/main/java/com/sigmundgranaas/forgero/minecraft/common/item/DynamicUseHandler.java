package com.sigmundgranaas.forgero.minecraft.common.item;

import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;
import com.sigmundgranaas.forgero.core.property.v2.feature.Feature;
import com.sigmundgranaas.forgero.minecraft.common.feature.OnUseFeature;
import com.sigmundgranaas.forgero.minecraft.common.handler.use.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

import java.util.Optional;

import static com.sigmundgranaas.forgero.minecraft.common.feature.FeatureUtils.cachedFeature;

public interface DynamicUseHandler extends EntityUseHandler, UseHandler, BlockUseHandler, StopHandler {

	default BaseHandler of(ItemStack stack) {
		var feature = cachedFeature(stack, OnUseFeature.KEY);
		if (feature.isPresent()) {
			return feature.get();
		}
		return this;
	}

	default <T extends Feature> Optional<T> of(ItemStack stack, ClassKey<T> key) {
		return cachedFeature(stack, key);
	}

	@Override
	default UseAction getUseAction(ItemStack stack) {
		return of(stack).getUseAction(stack);
	}

	@Override
	default int getMaxUseTime(ItemStack stack) {
		return of(stack).getMaxUseTime(stack);
	}

	@Override
	default void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
		of(stack).usageTick(world, user, stack, remainingUseTicks);
	}

	@Override
	default ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
		return of(stack).finishUsing(stack, world, user);
	}

	@Override
	default void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
		of(stack, OnUseFeature.KEY)
				.ifPresent(handler -> handler.onStoppedUsing(stack, world, user, remainingUseTicks));
	}

	@Override
	default boolean isUsedOnRelease(ItemStack stack) {
		return of(stack).isUsedOnRelease(stack);
	}

	@Override
	default ActionResult useOnBlock(ItemUsageContext context) {
		return of(context.getStack(), OnUseFeature.KEY)
				.map(handler -> handler.useOnBlock(context))
				.orElse(ActionResult.PASS);
	}

	@Override
	default ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
		return of(stack, OnUseFeature.KEY)
				.map(handler -> handler.useOnEntity(stack, user, entity, hand))
				.orElse(ActionResult.PASS);
	}

	@Override
	default TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		return of(user.getStackInHand(hand), OnUseFeature.KEY)
				.map(handler -> handler.use(world, user, hand))
				.orElse(TypedActionResult.pass(user.getStackInHand(hand)));
	}
}
