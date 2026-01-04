package com.sigmundgranaas.forgero.bows.handlers;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.bows.entity.DynamicArrowEntity;
import com.sigmundgranaas.forgero.bows.item.ForgeroArrowItem;
import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.useinteraction.UseContext;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeQueryResult;
import com.sigmundgranaas.forgero.core.attribute.impl.AttributeEngine;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import com.sigmundgranaas.forgero.loader.api.ForgeroInitializedCallback;
import com.sigmundgranaas.forgero.loader.api.ForgeroServices;
import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.ContextualUseHandler;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.world.World;

/**
 * Handler that launches a projectile when the bow is released.
 * This is typically used in the "on_release" phase of UseInteractionProperty.
 *
 * <p>The handler respects enchantments like Infinity and Power, and
 * properly handles creative mode and arrow consumption.</p>
 *
 * <h3>JSON Example:</h3>
 * <pre>
 * {
 *   "type": "forgero:launch_projectile",
 *   "base_power": 3.0,
 *   "base_divergence": 1.0
 * }
 * </pre>
 */
public record LaunchProjectileHandler(
		float basePower,
		float baseDivergence
) implements ContextualUseHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(LaunchProjectileHandler.class);
	public static final String TYPE = "forgero:launch_projectile";
	private static final float DEFAULT_POWER = 3.0f;
	private static final float DEFAULT_DIVERGENCE = 1.0f;
	private static final float DEFAULT_ACCURACY = 50.0f;

	// Services received from ForgeroInitializedCallback
	private static ComponentConverter converter;
	private static Resolver resolver;

	static {
		ForgeroInitializedCallback.EVENT.register(services -> {
			converter = services.converter();
			resolver = services.resolver();
			LOGGER.debug("LaunchProjectileHandler services initialized");
		});
	}

	/**
	 * Attribute identifier for resolving draw power from Forgero components.
	 * Higher draw power = faster projectile velocity.
	 */
	private static final OpenIdentifier DRAW_POWER_ATTR = new OpenIdentifier("forgero", "draw_power");

	/**
	 * Attribute identifier for resolving accuracy from Forgero components.
	 * Higher accuracy = lower divergence (more accurate shots).
	 */
	private static final OpenIdentifier ACCURACY_ATTR = new OpenIdentifier("forgero", "accuracy");

	public static final Codec<LaunchProjectileHandler> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					Codec.FLOAT.optionalFieldOf("base_power", DEFAULT_POWER).forGetter(LaunchProjectileHandler::basePower),
					Codec.FLOAT.optionalFieldOf("base_divergence", DEFAULT_DIVERGENCE).forGetter(LaunchProjectileHandler::baseDivergence)
			).apply(instance, LaunchProjectileHandler::new)
	);

	/**
	 * Determines whether to use DynamicArrowEntity for this arrow.
	 * Custom Forgero arrow components use DynamicArrowEntity to support custom models and properties.
	 *
	 * @param arrowStack The arrow ItemStack
	 * @return true if DynamicArrowEntity should be used, false for vanilla arrow entity
	 */
	private boolean shouldUseDynamicArrow(ItemStack arrowStack) {
		// Only use DynamicArrowEntity for ForgeroArrowItem instances
		// This ensures vanilla arrows always use vanilla arrow entities
		return arrowStack.getItem() instanceof ForgeroArrowItem;
	}

	@Override
	public void apply(UseContext context) {
		if (context.isClient()) {
			return;
		}

		context.asPlayer().ifPresent(player -> {
			ItemStack bowStack = context.stack();
			ItemStack arrowStack = getArrowStack(player, bowStack);

			if (arrowStack.isEmpty()) {
				LOGGER.debug("No arrow available for player {} to launch", player.getName().getString());
				return;
			}

			float pullProgress = context.pullProgress();
			if (pullProgress < 0.1f) {
				LOGGER.trace("Pull progress too low ({}) for player {}", pullProgress, player.getName().getString());
				return;
			}

			World world = context.world();

			// Automatic detection: use DynamicArrowEntity for Forgero arrows, vanilla for standard arrows
			PersistentProjectileEntity projectile;
			if (shouldUseDynamicArrow(arrowStack)) {
				// Create DynamicArrowEntity for custom Forgero arrows (supports custom models and properties)
				projectile = new DynamicArrowEntity(world, player, arrowStack);
				LOGGER.debug("Created DynamicArrowEntity for Forgero arrow: {}", arrowStack.getItem().getTranslationKey());
			} else {
				// Use vanilla arrow creation for standard arrows
				ArrowItem arrowItem = (arrowStack.getItem() instanceof ArrowItem ai) ? ai : (ArrowItem) Items.ARROW;
				projectile = arrowItem.createArrow(world, arrowStack, player);
				LOGGER.debug("Created vanilla arrow entity for: {}", arrowStack.getItem().getTranslationKey());
			}

			// Resolve draw power and accuracy from component, fall back to handler config
			float resolvedPower = resolveAttribute(bowStack, DRAW_POWER_ATTR, basePower);
			float resolvedAccuracy = resolveAttribute(bowStack, ACCURACY_ATTR, DEFAULT_ACCURACY);
			float divergence = accuracyToDivergence(resolvedAccuracy, baseDivergence);

			float velocity = resolvedPower * pullProgress;
			projectile.setVelocity(player, player.getPitch(), player.getYaw(), 0.0f, velocity, divergence);

			boolean isCritical = pullProgress >= 1.0f;
			if (isCritical) {
				projectile.setCritical(true);
			}

			applyEnchantments(bowStack, projectile, player);

			world.spawnEntity(projectile);

			playSound(world, player, pullProgress);

			LOGGER.debug("Launched projectile for player {}: velocity={}, divergence={}, critical={}, power={}, accuracy={}",
					player.getName().getString(), velocity, divergence, isCritical, resolvedPower, resolvedAccuracy);

			consumeArrow(player, bowStack, arrowStack);
			damageBow(player, bowStack, context.hand());
			player.incrementStat(Stats.USED.getOrCreateStat(bowStack.getItem()));
		});
	}

	private ItemStack getArrowStack(PlayerEntity player, ItemStack bowStack) {
		// IMPORTANT: We cannot use player.getProjectileType() because it creates a new ItemStack
		// instead of returning the actual one from inventory. This causes ForgeroArrowItem instances
		// to be converted to vanilla ArrowItem, breaking Forgero arrow detection.
		// Instead, we search the inventory directly for arrow items.

		// Check offhand first (vanilla behavior)
		ItemStack offhandStack = player.getOffHandStack();
		if (!offhandStack.isEmpty() && offhandStack.getItem() instanceof ArrowItem) {
			LOGGER.debug("Found arrow in offhand: {}", offhandStack.getItem().getTranslationKey());
			return offhandStack;
		}

		// Search main inventory for arrows
		for (int i = 0; i < player.getInventory().size(); i++) {
			ItemStack stack = player.getInventory().getStack(i);
			if (!stack.isEmpty() && stack.getItem() instanceof ArrowItem) {
				LOGGER.debug("Found arrow in inventory slot {}: {}", i, stack.getItem().getTranslationKey());
				return stack;
			}
		}

		// Creative mode fallback
		if (player.getAbilities().creativeMode) {
			LOGGER.debug("No arrows found in inventory, using creative mode fallback (vanilla arrow)");
			return new ItemStack(Items.ARROW);
		}

		LOGGER.debug("No arrows found for player {}", player.getName().getString());
		return ItemStack.EMPTY;
	}

	private void applyEnchantments(ItemStack bowStack, PersistentProjectileEntity projectile, PlayerEntity player) {
		int powerLevel = EnchantmentHelper.getLevel(Enchantments.POWER, bowStack);
		if (powerLevel > 0) {
			projectile.setDamage(projectile.getDamage() + (double) powerLevel * 0.5 + 0.5);
		}

		int punchLevel = EnchantmentHelper.getLevel(Enchantments.PUNCH, bowStack);
		if (punchLevel > 0) {
			projectile.setPunch(punchLevel);
		}

		if (EnchantmentHelper.getLevel(Enchantments.FLAME, bowStack) > 0) {
			projectile.setOnFireFor(100);
		}

		if (hasInfinity(bowStack) || player.getAbilities().creativeMode) {
			projectile.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
		}
	}

	private void playSound(World world, PlayerEntity player, float pullProgress) {
		world.playSound(
				null,
				player.getX(),
				player.getY(),
				player.getZ(),
				SoundEvents.ENTITY_ARROW_SHOOT,
				SoundCategory.PLAYERS,
				1.0f,
				1.0f / (world.getRandom().nextFloat() * 0.4f + 1.2f) + pullProgress * 0.5f
		);
	}

	private void consumeArrow(PlayerEntity player, ItemStack bowStack, ItemStack arrowStack) {
		if (!player.getAbilities().creativeMode && !hasInfinity(bowStack)) {
			arrowStack.decrement(1);
			if (arrowStack.isEmpty()) {
				player.getInventory().removeOne(arrowStack);
			}
		}
	}

	private void damageBow(PlayerEntity player, ItemStack bowStack, net.minecraft.util.Hand hand) {
		if (!player.getAbilities().creativeMode) {
			bowStack.damage(1, player, p -> p.sendToolBreakStatus(hand));
		}
	}

	private boolean hasInfinity(ItemStack stack) {
		return EnchantmentHelper.getLevel(Enchantments.INFINITY, stack) > 0;
	}

	/**
	 * Resolves an attribute value from the item's Forgero component.
	 * If the item is not a Forgero item or doesn't have the attribute, returns the fallback value.
	 *
	 * @param stack    The item stack to resolve from
	 * @param attr     The attribute identifier to query
	 * @param fallback The fallback value if resolution fails or returns zero/negative
	 * @return The resolved attribute value, or fallback if not available
	 */
	private static float resolveAttribute(ItemStack stack, OpenIdentifier attr, float fallback) {
		Optional<Float> result = converter.toComponent(stack)
				.map(component -> {
					AttributeQueryResult attrResult = resolver
							.resolve(component, new AttributeEngine());
					return attrResult.getValue(attr);
				})
				.filter(value -> value > 0);

		return result.orElse(fallback);
	}

	/**
	 * Converts accuracy (0-100 scale) to divergence (projectile spread).
	 * Higher accuracy = lower divergence = more accurate shots.
	 *
	 * @param accuracy      The accuracy value (0-100 scale, higher is better)
	 * @param fallbackDiv   The fallback divergence if accuracy is at default
	 * @return The divergence value (higher = less accurate)
	 */
	private static float accuracyToDivergence(float accuracy, float fallbackDiv) {
		if (accuracy == DEFAULT_ACCURACY) {
			return fallbackDiv;
		}
		// Convert accuracy (0-100) to divergence
		// 100 accuracy = 0 divergence, 0 accuracy = 10 divergence
		return Math.max(0.0f, (100f - accuracy) / 10f);
	}

	@Override
	public String type() {
		return TYPE;
	}
}
