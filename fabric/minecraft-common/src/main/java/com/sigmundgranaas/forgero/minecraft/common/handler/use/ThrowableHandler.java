package com.sigmundgranaas.forgero.minecraft.common.handler.use;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Weight;
import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
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

	private final ThrowableConfig config;

	public ThrowableHandler(ThrowableConfig config) {
		this.config = config;
	}

	public static ThrowableHandler fromJson(JsonObject json) {
		ThrowableConfig config = ThrowableConfig.fromJson(json);
		return new ThrowableHandler(config);
	}

	@Override
	public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
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
					config.spinType
			);

			throwableItem.setVelocity(
					user,
					user.getPitch(),
					user.getYaw(),
					0.0f,
					10 * config.velocityMultiplier(),
					config.instability()
			);
			world.spawnEntity(throwableItem);
		}
	}
	
	public record ThrowableConfig(float velocityMultiplier, float instability, ThrowableItem.SpinType spinType) {

		public static ThrowableConfig fromJson(JsonObject json) {
			float velocityMultiplier = json.has("velocity_multiplier") ? json.get("velocity_multiplier").getAsFloat() : 1.0F;
			float instability = json.has("instability") ? json.get("instability").getAsFloat() : 0.0F;
			ThrowableItem.SpinType spinType = json.has("spin_type") ? ThrowableItem.SpinType.valueOf(json.get("spin_type").getAsString()) : ThrowableItem.SpinType.NONE;

			return new ThrowableConfig(velocityMultiplier, instability, spinType);
		}
	}
}
