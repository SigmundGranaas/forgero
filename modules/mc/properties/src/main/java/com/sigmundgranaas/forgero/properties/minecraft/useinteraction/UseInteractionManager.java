package com.sigmundgranaas.forgero.properties.minecraft.useinteraction;

import java.util.List;
import java.util.Optional;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;
import com.sigmundgranaas.forgero.loader.api.ForgeroApi;
import com.sigmundgranaas.forgero.common.useinteraction.UseContext;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

/**
 * Manager that handles the lifecycle of use interactions.
 * This is the bridge between Minecraft's item use system and Forgero's UseInteractionProperty.
 *
 * <p>This manager follows the same patterns as {@link com.sigmundgranaas.forgero.properties.minecraft.onhit.OnHitManager}:</p>
 * <ul>
 *   <li>Static utility class with resolution logic</li>
 *   <li>Handler dispatch based on instanceof checks</li>
 *   <li>Property resolution via ForgeroApi</li>
 * </ul>
 *
 * <p>All methods are static and designed to be called from mixins.</p>
 */
public final class UseInteractionManager {

	private UseInteractionManager() {
		// Static utility class
	}

	/**
	 * Handles the start of a use action (right-click).
	 * Called from Item.use() mixin.
	 *
	 * @return TypedActionResult indicating if use should proceed
	 */
	public static TypedActionResult<ItemStack> handleUse(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);

		if (stack.isEmpty()) {
			return TypedActionResult.pass(stack);
		}

		Optional<UseInteractionProperty> property = getActiveProperty(stack, player);

		if (property.isEmpty()) {
			return TypedActionResult.pass(stack);
		}

		UseInteractionProperty prop = property.get();

		if (!prop.hasStartHandlers()) {
			return TypedActionResult.pass(stack);
		}

		// Execute start handlers
		executeHandlers(prop.onStart(), player, stack, hand, null);

		return new TypedActionResult<>(ActionResult.CONSUME, player.getStackInHand(hand));
	}

	/**
	 * Handles each tick while the item is being used.
	 * Called from Item.usageTick() mixin.
	 */
	public static void handleTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
		if (stack.isEmpty() || world.isClient()) {
			return;
		}

		Optional<UseInteractionProperty> property = getActiveProperty(stack, user);

		if (property.isEmpty() || !property.get().hasTickHandlers()) {
			return;
		}

		UseInteractionProperty prop = property.get();
		int chargeTime = prop.maxUseTime() - remainingUseTicks;
		float pullProgress = calculatePullProgress(chargeTime, prop.maxUseTime());

		Hand hand = user.getActiveHand();
		UseContext context = UseContext.tick(world, user, hand, stack, chargeTime, remainingUseTicks, pullProgress);

		executeHandlers(prop.onTick(), user, stack, hand, context);
	}

	/**
	 * Handles when the player stops using the item (releases the button).
	 * Called from Item.onStoppedUsing() mixin.
	 */
	public static void handleRelease(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
		if (stack.isEmpty() || world.isClient()) {
			return;
		}

		Optional<UseInteractionProperty> property = getActiveProperty(stack, user);

		if (property.isEmpty() || !property.get().hasReleaseHandlers()) {
			return;
		}

		UseInteractionProperty prop = property.get();
		int chargeTime = prop.maxUseTime() - remainingUseTicks;
		float pullProgress = calculatePullProgress(chargeTime, prop.maxUseTime());

		Hand hand = user.getActiveHand();
		UseContext context = UseContext.release(world, user, hand, stack, chargeTime, remainingUseTicks, pullProgress);

		executeHandlers(prop.onRelease(), user, stack, hand, context);
	}

	/**
	 * Handles when the item finishes being used (max time reached).
	 * Called from Item.finishUsing() mixin.
	 *
	 * @return the resulting ItemStack after use
	 */
	public static ItemStack handleFinish(ItemStack stack, World world, LivingEntity user) {
		if (stack.isEmpty() || world.isClient()) {
			return stack;
		}

		Optional<UseInteractionProperty> property = getActiveProperty(stack, user);

		if (property.isEmpty() || !property.get().hasFinishHandlers()) {
			return stack;
		}

		UseInteractionProperty prop = property.get();
		Hand hand = user.getActiveHand();
		UseContext context = UseContext.finish(world, user, hand, stack, prop.maxUseTime());

		executeHandlers(prop.onFinish(), user, stack, hand, context);

		return user instanceof PlayerEntity player ? player.getStackInHand(hand) : stack;
	}

	/**
	 * Gets the use action for an item.
	 * Called from Item.getUseAction() mixin.
	 */
	public static Optional<UseAction> getUseAction(ItemStack stack) {
		return getActiveProperty(stack, null)
				.filter(UseInteractionProperty::hasUseAction)
				.map(UseInteractionProperty::useAction);
	}

	/**
	 * Gets the max use time for an item.
	 * Called from Item.getMaxUseTime() mixin.
	 */
	public static Optional<Integer> getMaxUseTime(ItemStack stack) {
		return getActiveProperty(stack, null)
				.filter(UseInteractionProperty::hasUseAction)
				.map(UseInteractionProperty::maxUseTime);
	}

	/**
	 * Checks if the item is used on release vs finish.
	 * Called from Item.isUsedOnRelease() mixin.
	 */
	public static Optional<Boolean> isUsedOnRelease(ItemStack stack) {
		return getActiveProperty(stack, null)
				.filter(UseInteractionProperty::hasUseAction)
				.map(UseInteractionProperty::usedOnRelease);
	}

	/**
	 * Checks if the stack has a UseInteractionProperty.
	 */
	public static boolean hasUseInteraction(ItemStack stack) {
		return getActiveProperty(stack, null).isPresent();
	}

	/**
	 * Executes handlers using instanceof dispatch pattern (same as OnHitManager).
	 *
	 * @param handlers the handlers to execute
	 * @param user     the entity using the item
	 * @param stack    the item stack
	 * @param hand     the hand holding the item
	 * @param context  the use context (may be null for start phase)
	 */
	private static void executeHandlers(List<UseHandler> handlers, LivingEntity user, ItemStack stack, Hand hand, UseContext context) {
		for (UseHandler handler : handlers) {
			if (handler instanceof ContextualUseHandler contextual && context != null) {
				contextual.apply(context);
			} else if (handler instanceof SimpleUseHandler simple) {
				simple.apply(user, stack, hand);
			}
		}
	}

	/**
	 * Calculates pull progress (0.0 to 1.0) based on charge time.
	 */
	private static float calculatePullProgress(int chargeTime, int maxUseTime) {
		if (maxUseTime <= 0) {
			return 0f;
		}
		// Default to 1 second (20 ticks) for full charge
		int fullChargeTime = Math.min(maxUseTime, 20);
		return Math.min((float) chargeTime / fullChargeTime, 1.0f);
	}

	/**
	 * Gets the active UseInteractionProperty for an item stack.
	 */
	private static Optional<UseInteractionProperty> getActiveProperty(ItemStack stack, LivingEntity user) {
		if (stack.isEmpty()) {
			return Optional.empty();
		}

		return ForgeroApi.converter().toComponent(stack)
				.flatMap(component -> getProperties(component))
				.flatMap(list -> list.stream().findFirst());
	}

	/**
	 * Resolves UseInteractionProperty list from a component.
	 */
	private static Optional<List<UseInteractionProperty>> getProperties(Component component) {
		var engine = new UseInteractionProperty.Engine();
		DynamicContext context = new DynamicContext.Builder().build();

		List<UseInteractionProperty> properties = ForgeroApi.resolver().resolve(component, engine, context);

		return properties.isEmpty() ? Optional.empty() : Optional.of(properties);
	}
}
