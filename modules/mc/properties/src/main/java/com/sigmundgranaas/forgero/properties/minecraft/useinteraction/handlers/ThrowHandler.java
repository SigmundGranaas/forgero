package com.sigmundgranaas.forgero.properties.minecraft.useinteraction.handlers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.useinteraction.UseContext;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeQueryResult;
import com.sigmundgranaas.forgero.core.attribute.impl.AttributeEngine;
import com.sigmundgranaas.forgero.common.api.ForgeroInitializedCallback;
import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.ContextualUseHandler;
import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.entity.ThrownItemEntity;

import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.world.World;

/**
 * Handler that throws an item as a projectile when use is released.
 * This is typically used in the "on_release" phase of UseInteractionProperty.
 *
 * <p>The handler creates a {@link ThrownItemEntity} with configurable velocity,
 * instability (divergence), charge time, and spin type.</p>
 *
 * <h3>JSON Example (Spear):</h3>
 * <pre>
 * {
 *   "type": "forgero:throw",
 *   "velocity_multiplier": 1.0,
 *   "instability": 0.0,
 *   "charge_time": 20,
 *   "spin_type": "VERTICAL",
 *   "weight": 10.0
 * }
 * </pre>
 *
 * <h3>Full UseInteraction Example:</h3>
 * <pre>
 * {
 *   "minecraft:use_interaction": {
 *     "use_action": "SPEAR",
 *     "max_use_time": 72000,
 *     "used_on_release": true,
 *     "on_start": [
 *       { "type": "forgero:start_use" }
 *     ],
 *     "on_release": [
 *       { "type": "forgero:throw", "spin_type": "VERTICAL" },
 *       { "type": "forgero:consume_stack", "count": 1 }
 *     ]
 *   }
 * }
 * </pre>
 */
public record ThrowHandler(
		float velocityMultiplier,
		float instability,
		float chargeTime,
		String spinType,
		float weight
) implements ContextualUseHandler {

	public static final String TYPE = "forgero:throw";

	private static final float DEFAULT_VELOCITY_MULTIPLIER = 1.0f;
	private static final float DEFAULT_INSTABILITY = 0.0f;
	private static final float DEFAULT_CHARGE_TIME = 20f;
	private static final String DEFAULT_SPIN_TYPE = "NONE";
	private static final float DEFAULT_WEIGHT = 10f;

	// Services received from ForgeroInitializedCallback
	private static ComponentConverter converter;

	static {
		ForgeroInitializedCallback.EVENT.register(services -> {
			converter = services.converter();
		});
	}

	/**
	 * Attribute identifier for resolving weight from Forgero components.
	 * If the item has this attribute defined, it will be used instead of the handler's weight parameter.
	 */
	private static final OpenIdentifier WEIGHT_ATTR = new OpenIdentifier("forgero", "weight");

	public static final Codec<ThrowHandler> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					Codec.FLOAT.optionalFieldOf("velocity_multiplier", DEFAULT_VELOCITY_MULTIPLIER).forGetter(ThrowHandler::velocityMultiplier),
					Codec.FLOAT.optionalFieldOf("instability", DEFAULT_INSTABILITY).forGetter(ThrowHandler::instability),
					Codec.FLOAT.optionalFieldOf("charge_time", DEFAULT_CHARGE_TIME).forGetter(ThrowHandler::chargeTime),
					Codec.STRING.optionalFieldOf("spin_type", DEFAULT_SPIN_TYPE).forGetter(ThrowHandler::spinType),
					Codec.FLOAT.optionalFieldOf("weight", DEFAULT_WEIGHT).forGetter(ThrowHandler::weight)
			).apply(instance, ThrowHandler::new)
	);

	@Override
	public void apply(UseContext context) {
		if (context.isClient()) {
			return;
		}

		World world = context.world();
		ItemStack stack = context.stack();

		if (stack.isEmpty()) {
			return;
		}

		float chargeProgress = calculateChargeProgress(context);
		if (chargeProgress <= 0) {
			return;
		}

		ThrownItemEntity.SpinType spin = parseSpinType(spinType);

		// Resolve weight from component if available, otherwise use handler's configured weight
		float resolvedWeight = resolveAttribute(stack, WEIGHT_ATTR, weight);

		ThrownItemEntity thrownItem = new ThrownItemEntity(
				ThrownItemEntityRegistry.THROWN_ITEM_ENTITY,
				world,
				context.user(),
				stack.copy(),
				resolvedWeight,
				spin
		);

		float velocity = 2.5f * velocityMultiplier * chargeProgress;
		thrownItem.setVelocity(
				context.user(),
				context.user().getPitch(),
				context.user().getYaw(),
				0.0f,
				velocity,
				instability
		);

		thrownItem.setPosition(
				thrownItem.getX(),
				thrownItem.getY() + 0.5,
				thrownItem.getZ()
		);

		if (context.asPlayer().map(p -> p.getAbilities().creativeMode).orElse(false)) {
			thrownItem.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
		} else {
			thrownItem.pickupType = PersistentProjectileEntity.PickupPermission.ALLOWED;
		}

		world.spawnEntity(thrownItem);

		playThrowSound(context);

		context.asPlayer().ifPresent(player -> {
			player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
		});
	}

	private float calculateChargeProgress(UseContext context) {
		int maxUseTime = 72000;
		int chargeTimeTicks = maxUseTime - context.remainingTicks();
		if (chargeTimeTicks <= 0) {
			chargeTimeTicks = context.chargeTime();
		}
		return Math.max(Math.min(chargeTimeTicks / chargeTime, 1.0f), 0.0f);
	}

	private ThrownItemEntity.SpinType parseSpinType(String spinTypeName) {
		try {
			return ThrownItemEntity.SpinType.valueOf(spinTypeName.toUpperCase());
		} catch (IllegalArgumentException e) {
			return ThrownItemEntity.SpinType.NONE;
		}
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
		return converter.toComponent(stack)
				.map(component -> {
					AttributeQueryResult result = new AttributeEngine().resolve(component);
					return result.getValue(attr);
				})
				.filter(value -> value > 0)
				.orElse(fallback);
	}

	private void playThrowSound(UseContext context) {
		context.asPlayer().ifPresent(player -> {
			context.world().playSound(
					null,
					player.getX(),
					player.getY(),
					player.getZ(),
					SoundEvents.ITEM_TRIDENT_THROW,
					SoundCategory.PLAYERS,
					1.0f,
					1.0f
			);
		});
	}

	@Override
	public String type() {
		return TYPE;
	}
}
