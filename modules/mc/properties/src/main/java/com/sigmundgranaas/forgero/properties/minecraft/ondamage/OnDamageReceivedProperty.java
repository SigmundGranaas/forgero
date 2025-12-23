package com.sigmundgranaas.forgero.properties.minecraft.ondamage;

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
 * The Orchestrator property for On-Damage-Received events. This triggers when the wielder
 * takes damage, enabling defensive effects like thorns, counter-attacks, and protective buffs.
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
 *       "amount": 2.0
 *     },
 *     {
 *       "type": "forgero:knockback",
 *       "force": 1.5,
 *       "direction": "push"
 *     }
 *   ],
 *   "condition": {
 *     "type": "forgero:is_sneaking"
 *   }
 * }
 * </pre>
 *
 * <h3>Use Cases:</h3>
 * <ul>
 *   <li>Thorns effects - damage attackers when hit</li>
 *   <li>Counter-knockback - push attackers away</li>
 *   <li>Defensive buffs - gain resistance when damaged</li>
 *   <li>Heal on hit taken - life steal from attackers</li>
 * </ul>
 *
 * <h3>Important Notes:</h3>
 * <ul>
 *   <li>Defender is the "source" entity in effect application</li>
 *   <li>Attacker is the "target" entity for effects</li>
 *   <li>Environmental damage (no attacker) is ignored</li>
 *   <li>Works with armor and held items</li>
 * </ul>
 *
 * @param selector The entity selector for determining which entities are affected
 * @param effects The list of effects to apply when damage is received
 * @param condition Optional condition that must be met for effects to apply
 */
public record OnDamageReceivedProperty(
		EntitySelector selector,
		List<OnHitEffect> effects,
		@Nullable Condition condition
) implements ConditionalProperty {
	public static final OpenIdentifier KEY_ID = new OpenIdentifier("minecraft", "on_damage_received");
	public static final ResolutionKey<List<OnDamageReceivedProperty>> KEY = new ResolutionKey<>(KEY_ID);
	public static final PropertyKey<OnDamageReceivedProperty> PROPERTY_KEY = new PropertyKey<>(OnDamageReceivedProperty.class, KEY_ID.toString());

	public static Codec<OnDamageReceivedProperty> codec(Codec<Condition> conditionCodec) {
		return RecordCodecBuilder.create(instance -> instance.group(
				EntitySelector.CODEC.fieldOf("selector").forGetter(OnDamageReceivedProperty::selector),
				Codec.list(OnHitEffect.CODEC).fieldOf("effects").forGetter(OnDamageReceivedProperty::effects),
				conditionCodec.optionalFieldOf("condition").forGetter(p -> Optional.ofNullable(p.condition()))
		).apply(instance, (selector, effects, condition) -> new OnDamageReceivedProperty(selector, effects, condition.orElse(null))));
	}

	@Override
	public @Nullable Condition condition() {
		return condition;
	}

	public static class Engine extends AbstractConditionalPropertyEngine<OnDamageReceivedProperty, List<OnDamageReceivedProperty>> {
		public Engine() {
			super(KEY, PROPERTY_KEY);
		}

		@Override
		public List<OnDamageReceivedProperty> apply(OptimizedBakedResult<OnDamageReceivedProperty> baked, DynamicContext context) {
			return baked.stream(context).collect(Collectors.toList());
		}
	}
}
