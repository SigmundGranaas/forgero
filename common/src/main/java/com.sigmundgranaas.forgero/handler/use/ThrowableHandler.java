package com.sigmundgranaas.forgero.handler.use;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Weight;
import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;
import com.sigmundgranaas.forgero.service.StateService;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * Represents a handler that throws an item when the player stops using it.
 * <p>
 * Example JSON configuration:
 * <pre>
 *    {
 *     "type": "minecraft:on_use",
 *     "max_use_time": 72000,
 *     "use_action": "SPEAR",
 *     "use": [
 *      {
 *          "type": "minecraft:consume"
 *      }
 *     ],
 *     "on_stop": [
 *      {
 *          "type": "forgero:throw",
 *          "spin_type": "VERTICAL"
 *      },
 *      {
 *          "type": "minecraft:play_sound",
 *          "sound": "minecraft:item.trident.throw"
 *      },
 *      {
 *          "type": "minecraft:consume_stack",
 *          "count": 1
 *      }
 *     ]
 *    }
 * </pre>
 * </p>
 */
public class ThrowableHandler implements StopHandler {
	public static final String TYPE = "forgero:throw";
	public static final ClassKey<ThrowableHandler> KEY = new ClassKey<>(TYPE, ThrowableHandler.class);
	public static final JsonBuilder<ThrowableHandler> BUILDER = HandlerBuilder.fromObject(ThrowableHandler.class, ThrowableHandler::fromJson);

	private final float velocityMultiplier;
	private final float instability;
	private final float chargeTime;
	private final ThrowableItem.SpinType spinType;

	public ThrowableHandler(float velocityMultiplier, float instability, float chargeTime, ThrowableItem.SpinType spinType) {
		this.velocityMultiplier = velocityMultiplier;
		this.instability = instability;
		this.chargeTime = chargeTime;
		this.spinType = spinType;
	}


	public static ThrowableHandler fromJson(JsonObject json) {
		float velocityMultiplier = json.has("velocity_multiplier") ? json.get("velocity_multiplier").getAsFloat() : 1.0F;
		float instability = json.has("instability") ? json.get("instability").getAsFloat() : 0.0F;
		float chargeTime = json.has("charge_time") ? json.get("charge_time").getAsFloat() : 20;
		ThrowableItem.SpinType spinType = json.has("spin_type") ? ThrowableItem.SpinType.valueOf(json.get("spin_type").getAsString()) : ThrowableItem.SpinType.NONE;
		return new ThrowableHandler(velocityMultiplier, instability, chargeTime, spinType );
	}

	@Override
	public void stoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
		float tickDelta = Math.max(Math.min((72000 - remainingUseTicks) / chargeTime, 1), 0);

		if (!world.isClient) {
			float weight = StateService.INSTANCE.convert(stack)
					.map(container -> ComputedAttribute.of(container, Weight.KEY))
					.map(ComputedAttribute::asFloat)
					.orElse(10f);

			ThrowableItem throwableItem = new ThrowableItem(
					world,
					user,
					stack.copy(),
					weight,
					spinType
			);

			throwableItem.setVelocity(
					user,
					user.getPitch(),
					user.getYaw(),
					0.0f,
					(10 * velocityMultiplier) * tickDelta,
					instability
			);
			throwableItem.setPosition(throwableItem.getX(), throwableItem.getY() + 0.5, throwableItem.getZ());
			world.spawnEntity(throwableItem);
		}
	}
}
