package com.sigmundgranaas.forgero.properties.minecraft.onkill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.property.api.PropertyKey;
import com.sigmundgranaas.forgero.core.property.api.ResolutionKey;
import com.sigmundgranaas.forgero.core.property.api.custom.AbstractConditionalPropertyEngine;
import com.sigmundgranaas.forgero.core.property.api.custom.ConditionalProperty;
import com.sigmundgranaas.forgero.core.property.api.custom.OptimizedBakedResult;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;
import com.sigmundgranaas.forgero.effects.entity.OnHitEffect;
import com.sigmundgranaas.forgero.properties.minecraft.entityselector.EntitySelector;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The Orchestrator property for On-Kill events. This triggers when the wielder
 * kills an entity, enabling victory rewards and "on kill" effects.
 *
 * <h3>JSON Configuration Example:</h3>
 * <pre>
 * {
 *   "selector": {
 *     "type": "forgero:single_target"
 *   },
 *   "effects": [
 *     {
 *       "type": "forgero:life_steal",
 *       "amount": 3.0
 *     },
 *     {
 *       "type": "forgero:spawn_entity",
 *       "entity": "minecraft:experience_orb",
 *       "count": 5,
 *       "on_target": false
 *     }
 *   ],
 *   "condition": {
 *     "type": "forgero:random_chance",
 *     "chance": 0.5
 *   }
 * }
 * </pre>
 *
 * <h3>Use Cases:</h3>
 * <ul>
 *   <li>Heal on kill - restore health after successful kills</li>
 *   <li>Gain buffs - apply status effects on kill</li>
 *   <li>Spawn minions - summon allies when enemies are defeated</li>
 *   <li>Experience boost - spawn extra XP orbs</li>
 *   <li>Chain reactions - explosions or effects on nearby enemies</li>
 * </ul>
 *
 * <h3>Important Notes:</h3>
 * <ul>
 *   <li>Distinct from on_hit - only triggers when entity dies</li>
 *   <li>Killer is the "source" entity in effect application</li>
 *   <li>Victim is the "target" entity for effects</li>
 *   <li>Works with environmental kills if attacker is tracked</li>
 *   <li>Indirect kills (poison, fire after hit) still trigger</li>
 * </ul>
 *
 * @param selector The entity selector for determining which entities are affected
 * @param effects The list of effects to apply when a kill occurs
 * @param condition Optional condition that must be met for effects to apply
 */
public record OnKillProperty(
		EntitySelector selector,
		List<OnHitEffect> effects,
		@Nullable Condition condition
) implements ConditionalProperty {
	public static final OpenIdentifier KEY_ID = new OpenIdentifier("minecraft", "on_kill");
	public static final ResolutionKey<List<OnKillProperty>> KEY = new ResolutionKey<>(KEY_ID);
	public static final PropertyKey<OnKillProperty> PROPERTY_KEY = new PropertyKey<>(OnKillProperty.class, KEY_ID.toString());

	public static Codec<OnKillProperty> codec(Codec<Condition> conditionCodec) {
		return RecordCodecBuilder.create(instance -> instance.group(
				EntitySelector.CODEC.fieldOf("selector").forGetter(OnKillProperty::selector),
				Codec.list(OnHitEffect.CODEC).fieldOf("effects").forGetter(OnKillProperty::effects),
				conditionCodec.optionalFieldOf("condition").forGetter(p -> Optional.ofNullable(p.condition()))
		).apply(instance, (selector, effects, condition) -> new OnKillProperty(selector, effects, condition.orElse(null))));
	}

	@Override
	public @Nullable Condition condition() {
		return condition;
	}

	public static class Engine extends AbstractConditionalPropertyEngine<OnKillProperty, List<OnKillProperty>> {
		public Engine() {
			super(KEY, PROPERTY_KEY);
		}

		@Override
		public List<OnKillProperty> apply(OptimizedBakedResult<OnKillProperty> baked, DynamicContext context) {
			return baked.stream(context).collect(Collectors.toList());
		}
	}
}
