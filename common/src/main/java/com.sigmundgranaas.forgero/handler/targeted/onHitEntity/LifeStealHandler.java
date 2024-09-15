package com.sigmundgranaas.forgero.handler.targeted.onHitEntity;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;
import com.sigmundgranaas.forgero.feature.ModifiableFeatureAttribute;
import lombok.Getter;
import lombok.experimental.Accessors;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

/**
 * Handler for applying lifesteal when hitting an entity.
 * The amount of lifesteal can be configured.
 *
 * <p>Example JSON configuration:
 * <pre>
 * {
 *   "type": "minecraft:on_hit",
 *   "on_hit": {
 *   "type": "forgero:life_steal",
 *     "target": "minecraft:targeted_entity",
 *     "damage": 1,
 *     "healing": 1,
 *   }
 * }
 *
 * The amount is configurable using attributes the attribute "forgero:life_steal".
 * </pre>
 * </p>
 */
@Getter
@Accessors(fluent = true)
public class LifeStealHandler implements EntityTargetHandler {

	public static final String TYPE = "forgero:life_steal";
	public static final JsonBuilder<LifeStealHandler> BUILDER = HandlerBuilder.fromObject(LifeStealHandler.class, LifeStealHandler::fromJson);

	public static final String AMOUNT_KEY = "amount";

	public static final ModifiableFeatureAttribute.Builder MODIFIER_BUILDER = ModifiableFeatureAttribute
			.builder(TYPE)
			.key(AMOUNT_KEY)
			.defaultValue(1);

	private final String target;
	private final ModifiableFeatureAttribute amount;

	/**
	 * Constructs a new {@link LifeStealHandler} with the specified target and amount of health to steal.
	 *
	 * @param target The target entity.
	 * @param amount The amount of health to steal.
	 */
	public LifeStealHandler(String target, ModifiableFeatureAttribute amount) {
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
		ModifiableFeatureAttribute amount = MODIFIER_BUILDER.build(json);
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

				float lifeSteal = amount.with(targetEntity).asFloat();
				float healthToSteal = Math.min(livingTarget.getHealth(), lifeSteal);

				livingTarget.damage(targetEntity.getDamageSources().magic(), healthToSteal); // Deduct health from target
				livingSource.heal(healthToSteal); // Add health to source
			}
		}
	}
}
