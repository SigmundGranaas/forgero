package com.sigmundgranaas.forgero.minecraft.common.handler.use;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

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
			ThrowableItem throwableItem = new ThrowableItem(
					world,
					user,
					stack.copy(),
					config.weight,
					config.spinType
			);

			throwableItem.setVelocity(
					user,
					user.getPitch(),
					user.getYaw(),
					0.0f,
					10 * config.velocityMultiplier(),
					1.0f
			);
			world.spawnEntity(throwableItem);
		}
	}

	@Getter
	@Accessors(fluent = true)
	public static class ThrowableConfig {
		private final float velocityMultiplier;
		private final float weight;
		private final ThrowableItem.SpinType spinType;

		public ThrowableConfig(float velocityMultiplier, float weight, ThrowableItem.SpinType spinType) {
			this.velocityMultiplier = velocityMultiplier;
			this.weight = weight;
			this.spinType = spinType;
		}

		public static ThrowableConfig fromJson(JsonObject json) {
			float velocityMultiplier = json.has("velocity_multiplier") ? json.get("velocity_multiplier").getAsFloat() : 1.0F;
			float weight = json.has("weight") ? json.get("weight").getAsFloat() : 1.0F;
			ThrowableItem.SpinType spinType = json.has("spin_type") ? ThrowableItem.SpinType.valueOf(json.get("spin_type").getAsString()) : ThrowableItem.SpinType.NONE;

			return new ThrowableConfig(velocityMultiplier, weight, spinType);
		}
	}
}
