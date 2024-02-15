package com.sigmundgranaas.forgero.minecraft.common.item;

import static com.sigmundgranaas.forgero.minecraft.common.feature.FeatureUtils.cachedFilteredFeature;
import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.*;

import java.util.Objects;
import java.util.Optional;

import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;
import com.sigmundgranaas.forgero.core.property.v2.feature.Feature;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.feature.OnUseFeature;
import com.sigmundgranaas.forgero.minecraft.common.handler.use.BaseHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.use.BlockUseHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.use.EntityUseHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.use.StopHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.use.UseHandler;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

/**
 * The DynamicItemUseHandler interface is designed to facilitate dynamic handling of
 * various use actions on items. It extends multiple handler interfaces
 * to cover a broad spectrum of interactions, including entity use, block use, and
 * generic use and afterUse actions.
 * <p>
 * Implementing this interface in an item class automatically delegates the handling
 * of use actions to the {@link OnUseFeature} system. This system allows for dynamic and
 * context-sensitive behavior based on the state of the item, the world, and the entity
 * interacting with the item.
 *
 * <p>Key Methods and Their Role:</p>
 * <ul>
 * <li><b>of:</b> Determines the appropriate handler based on the item stack and the
 *     current context, falling back to a default handler if no specific feature is found.</li>
 * <li><b>dynamicX:</b> Methods such as dynamicUseOnBlock, dynamicUseOnEntity, etc.,
 *     provide default implementations for their respective actions. They leverage the
 *     'of' method to fetch the context-specific handler and execute the action.
 *     The keyword is used in cases where a Mixin implementation would call its own methods, leading to infinite recursion. </li>
 * </ul>
 *
 * <p>Usage:</p>
 * When an item class implements this interface, its use-related actions are processed
 * through the OnUseFeature system. This means that the behavior of these actions can
 * be dynamically altered based on the item's state, player interaction, and other
 * internal state factors. The actual onUse features are described in Json files and are loaded into Forgero's state system.
 *
 * <p>Example:</p>
 * <pre>{@code
 * public class DynamicPickaxeItem extends PickaxeItem implements DynamicItemUseHandler {
 * }</pre>
 * <p>
 * In this example, a dynamic pickaxe item is created that can have custom behavior
 * when used on entities, blocks, or in general use scenarios.
 */
public interface DynamicItemUseHandler extends EntityUseHandler, UseHandler, BlockUseHandler, StopHandler {

	default BaseHandler of(ItemStack stack, MatchContext context) {
		var feature = cachedFilteredFeature(stack, OnUseFeature.KEY, context);
		if (feature.isPresent()) {
			return feature.get();
		}
		return BaseHandler.DEFAULT;
	}

	default <T extends Feature> Optional<T> of(ItemStack stack, ClassKey<T> key, MatchContext context) {
		return cachedFilteredFeature(stack, key, context);
	}

	@Override
	default UseAction getUseAction(ItemStack stack) {
		return dynamicGetUseAction(stack);
	}

	@Override
	default int getMaxUseTime(ItemStack stack) {
		return dynamicGetMaxUseTime(stack);
	}

	@Override
	default void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
		dynamicUsageTick(world, user, stack, remainingUseTicks);
	}

	@Override
	default ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
		return dynamicFinishUsing(stack, world, user);
	}

	@Override
	default void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
		dynamicOnStoppedUsing(stack, world, user, remainingUseTicks);
	}

	@Override
	default boolean isUsedOnRelease(ItemStack stack) {
		return dynamicIsUsedOnRelease(stack);
	}

	@Override
	default ActionResult useOnBlock(ItemUsageContext context) {
		return dynamicUseOnBlock(context);
	}

	@Override
	default ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
		return dynamicUseOnEntity(stack, user, entity, hand);
	}

	@Override
	default TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		return dynamicUse(world, user, hand);
	}


	default UseAction dynamicGetUseAction(ItemStack stack) {
		return of(stack, MatchContext.of().put(STACK, stack)).getUseAction(stack);
	}


	default int dynamicGetMaxUseTime(ItemStack stack) {
		return of(stack, MatchContext.of().put(STACK, stack)).getMaxUseTime(stack);
	}


	default void dynamicUsageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
		of(
				stack,
				MatchContext.of()
						.put(STACK, stack)
						.put(WORLD, world)
						.put(ENTITY, user)
		)
				.usageTick(world, user, stack, remainingUseTicks);
	}


	default ItemStack dynamicFinishUsing(ItemStack stack, World world, LivingEntity user) {
		return of(
				stack,
				MatchContext.of()
						.put(STACK, stack)
						.put(WORLD, world)
						.put(ENTITY, user)
		)
				.finishUsing(stack, world, user);
	}

	default void dynamicOnStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
		of(stack,
				OnUseFeature.KEY,
				MatchContext.of()
						.put(STACK, stack)
						.put(WORLD, world)
						.put(ENTITY, user)
		)
				.ifPresent(handler -> handler.onStoppedUsing(stack, world, user, remainingUseTicks));
	}


	default boolean dynamicIsUsedOnRelease(ItemStack stack) {
		return of(stack, MatchContext.of().put(STACK, stack)).isUsedOnRelease(stack);
	}


	default ActionResult dynamicUseOnBlock(ItemUsageContext context) {
		return of(context.getStack(),
				OnUseFeature.KEY,
				MatchContext.of()
						.put(ENTITY, context.getPlayer())
						.put(WORLD, Objects.requireNonNull(context.getPlayer()).getWorld())
						.put(BLOCK_TARGET, context.getBlockPos()))
				.map(handler -> handler.useOnBlock(context))
				.orElse(ActionResult.PASS);
	}


	default ActionResult dynamicUseOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
		return of(stack,
				OnUseFeature.KEY,
				MatchContext.of()
						.put(STACK, stack)
						.put(WORLD, user.getWorld())
						.put(ENTITY, user))
				.map(handler -> handler.useOnEntity(stack, user, entity, hand))
				.orElse(ActionResult.PASS);
	}


	default TypedActionResult<ItemStack> dynamicUse(World world, PlayerEntity user, Hand hand) {
		return of(user.getStackInHand(hand),
				OnUseFeature.KEY,
				MatchContext.of()
						.put(STACK, user.getStackInHand(hand))
						.put(WORLD, world)
						.put(ENTITY, user)
		)
				.map(handler -> handler.use(world, user, hand))
				.orElse(TypedActionResult.pass(user.getStackInHand(hand)));
	}
}
