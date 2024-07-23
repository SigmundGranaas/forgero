package com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitEntity;

import static com.sigmundgranaas.forgero.minecraft.common.feature.FeatureUtils.compute;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.core.property.Attribute;
import com.sigmundgranaas.forgero.core.property.attribute.BaseAttribute;
import lombok.Getter;
import lombok.experimental.Accessors;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
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
 *     "effect": "minecraft:poison",
 *     "level": 1
 *   },
 *   "predicate": {
 *     "type": "forgero:random",
 *     "chance": 0.5
 *   }
 * }
 * </pre>
 * </p>
 */
@Getter
@Accessors(fluent = true)
public class StatusEffectHandler implements EntityTargetHandler {
	public static final String TYPE = "minecraft:status_effect";
	public static final String EFFECT_LEVEL_ATTRIBUTE_TYPE = "minecraft:effect_level";
	public static final String EFFECT_DURATION_TYPE = "minecraft:effect_duration";

	private static final Attribute DEFAULT_LEVEL = BaseAttribute.of(1, EFFECT_LEVEL_ATTRIBUTE_TYPE);
	private static final Attribute DEFAULT_DURATION = BaseAttribute.of(20 * 30, EFFECT_DURATION_TYPE);

	public static Codec<StatusEffect> STATUS_EFFECT_CODEC = Identifier.CODEC.xmap(Registries.STATUS_EFFECT::get, Registries.STATUS_EFFECT::getId);

	public static final Codec<StatusEffectHandler> BUILDER = codec();


	private final StatusEffect effect;
	private final Attribute level;
	private final Attribute duration; // in ticks
	private final String target;

	/**
	 * Constructs a new {@link StatusEffectHandler} with the specified properties.
	 *
	 * @param effect   The type of status effect.
	 * @param level    The amplifier level of the effect.
	 * @param duration Duration in ticks for the effect.
	 * @param target   The target entity.
	 */
	public StatusEffectHandler(StatusEffect effect, Attribute level, Attribute duration, String target) {
		this.effect = effect;
		this.level = level;
		this.duration = duration;
		this.target = target;
	}

	public static Codec<StatusEffectHandler> codec() {
		return RecordCodecBuilder.create(instance -> instance.group(
				STATUS_EFFECT_CODEC.fieldOf("effect").forGetter(StatusEffectHandler::effect),
				Attribute.defaultOrExplicitTypeCodec(EFFECT_LEVEL_ATTRIBUTE_TYPE).optionalFieldOf("level", DEFAULT_LEVEL).forGetter(StatusEffectHandler::level),
				Attribute.defaultOrExplicitTypeCodec(EFFECT_DURATION_TYPE).optionalFieldOf("duration", DEFAULT_DURATION).forGetter(StatusEffectHandler::duration),
				Codec.STRING.fieldOf("target").forGetter(StatusEffectHandler::target)
		).apply(instance, StatusEffectHandler::new));
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
			livingTarget.addStatusEffect(new StatusEffectInstance(effect, duration(source), level(source) - 1));
		}
		else if ("minecraft:self".equals(target) && source instanceof LivingEntity sourceEntity) {
		sourceEntity.addStatusEffect(new StatusEffectInstance(effect, duration(source), level(source) - 1));
		}
	}


	private int duration(Entity source) {
		return compute(duration, source).asInt();
	}

	private int level(Entity source) {
		return compute(level, source).asInt();
	}
}


