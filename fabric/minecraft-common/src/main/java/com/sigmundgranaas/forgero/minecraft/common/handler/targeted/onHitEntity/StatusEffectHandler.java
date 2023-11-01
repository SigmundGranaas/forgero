package com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitEntity;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;
import lombok.Getter;
import lombok.experimental.Accessors;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;


/**
 * Represents a handler that applies a status effect to entities upon hitting a target.
 * The handler can be configured using a JSON format.
 *
 * <p>Example JSON configuration:
 * <pre>
 * {
 *   "type": "minecraft:on_hit",
 *   "on_hit": {
 *     "type": "minecraft:status_effect",
 *     "target": "minecraft:targeted_entity",
 *     "effect": {
 *       "type": "minecraft:poison",
 *       "level": 1
 *     }
 *   },
 *   "predicate": {
 *     "type": "forgero:chance",
 *     "chance": 0.5
 *   }
 * }
 * </pre>
 * </p>
 */
@Getter
@Accessors(fluent = true)
public class StatusEffectHandler implements OnHitHandler {

	public static final String TYPE = "minecraft:status_effect";
	// Default value
	private static final int DEFAULT_DURATION = 20 * 30;
	public static final JsonBuilder<OnHitHandler> BUILDER = HandlerBuilder.fromObject(OnHitHandler.KEY.clazz(), StatusEffectHandler::fromJson);
	private final StatusEffect effect;
	private final int level;
	private final int duration; // in ticks
	private final String target;

	/**
	 * Constructs a new {@link StatusEffectHandler} with the specified properties.
	 *
	 * @param effect   The type of status effect.
	 * @param level    The amplifier level of the effect.
	 * @param duration Duration in ticks for the effect.
	 * @param target   The target entity.
	 */
	public StatusEffectHandler(StatusEffect effect, int level, int duration, String target) {
		this.effect = effect;
		this.level = level;
		this.duration = duration;
		this.target = target;
	}


	/**
	 * Constructs an {@link StatusEffectHandler} from a JSON object.
	 *
	 * @param json The JSON object.
	 * @return A new instance of {@link StatusEffectHandler}.
	 */
	public static StatusEffectHandler fromJson(JsonObject json) {
		StatusEffect effect = Registry.STATUS_EFFECT.get(new Identifier(json.getAsJsonObject("effect").get("type").getAsString()));
		int level = json.getAsJsonObject("effect").get("level").getAsInt();
		int duration = json.getAsJsonObject("effect").has("duration") ? json.getAsJsonObject("effect").get("duration").getAsInt() : DEFAULT_DURATION;
		String target = json.get("target").getAsString();
		return new StatusEffectHandler(effect, level, duration, target);
	}

	/**
	 * This method is triggered upon hitting an entity.
	 * It checks the target specification and applies the status effect if conditions are met.
	 *
	 * @param source       The source entity.
	 * @param world        The world where the event occurred.
	 * @param targetEntity The targeted entity.
	 */
	@Override
	public void onHit(Entity source, World world, Entity targetEntity) {
		if ("minecraft:targeted_entity".equals(target) && targetEntity instanceof LivingEntity livingTarget) {
			livingTarget.addStatusEffect(new StatusEffectInstance(effect, duration, level - 1));
		}
	}
}


