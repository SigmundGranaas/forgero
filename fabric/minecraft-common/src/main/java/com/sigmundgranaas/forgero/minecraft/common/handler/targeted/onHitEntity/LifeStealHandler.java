package com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitEntity;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;
import lombok.Getter;
import lombok.experimental.Accessors;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.world.World;

@Getter
@Accessors(fluent = true)
public class LifeStealHandler implements EntityTargetHandler {

	public static final String TYPE = "forgero:life_steal";
	public static final JsonBuilder<EntityTargetHandler> BUILDER = HandlerBuilder.fromObject(EntityTargetHandler.KEY.clazz(), LifeStealHandler::fromJson);

	private final String target;
	private final float amount;

	/**
	 * Constructs a new {@link LifeStealHandler} with the specified target and amount of health to steal.
	 *
	 * @param target The target entity.
	 * @param amount The amount of health to steal.
	 */
	public LifeStealHandler(String target, float amount) {
		this.target = target;
		this.amount = amount;
	}

	/**
	 * Constructs an {@link LifeStealHandler} from a JSON object.
	 *
	 * @param json The JSON object.
	 * @return A new instance of {@link LifeStealHandler}.
	 */
	public static LifeStealHandler fromJson(JsonObject json) {
		String target = json.get("target").getAsString();
		float amount = json.get("amount").getAsFloat();
		return new LifeStealHandler(target, amount);
	}

	/**
	 * This method is triggered upon hitting an entity.
	 * It checks the target specification and steals health from the target if conditions are met.
	 *
	 * @param source       The source entity.
	 * @param world        The world where the event occurred.
	 * @param targetEntity The targeted entity.
	 */
	@Override
	public void onHit(Entity source, World world, Entity targetEntity) {
		if ("minecraft:targeted_entity".equals(target) && !world.isClient) {
			if (targetEntity instanceof LivingEntity livingTarget && source instanceof LivingEntity livingSource) {

				float healthToSteal = Math.min(livingTarget.getHealth(), amount);

				livingTarget.damage(targetEntity.getDamageSources().magic(), healthToSteal); // Deduct health from target
				livingSource.heal(healthToSteal); // Add health to source
			}
		}
	}
}
