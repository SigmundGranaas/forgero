package com.sigmundgranaas.forgero.properties.minecraft.onhit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.property.api.PropertyKey;
import com.sigmundgranaas.forgero.core.property.api.ResolutionKey;
import com.sigmundgranaas.forgero.core.property.api.custom.AbstractConditionalPropertyEngine;
import com.sigmundgranaas.forgero.core.property.api.custom.ConditionalProperty;
import com.sigmundgranaas.forgero.core.property.api.custom.OptimizedBakedResult;
import com.sigmundgranaas.forgero.effects.entity.OnHitEffect;
import com.sigmundgranaas.forgero.properties.minecraft.entityselector.EntitySelector;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The Orchestrator property for On-Hit events. It defines the "when" (on entity hit),
 * the "who" (via a selector with filters), and the "what" (via a list of effects).
 *
 * <h3>JSON Configuration Example:</h3>
 * <pre>
 * {
 *   "selector": {
 *     "type": "forgero:aoe",
 *     "radius": 2,
 *     "filters": [
 *       { "type": "forgero:is_hostile" },
 *       { "type": "forgero:health_threshold", "threshold": 0.5, "comparator": "less_than" }
 *     ]
 *   },
 *   "effects": [
 *     { "type": "forgero:lightning" },
 *     { "type": "forgero:life_steal", "amount": 1 }
 *   ],
 *   "condition": {
 *     "type": "forgero:is_sneaking"
 *   }
 * }
 * </pre>
 */
public record OnHitProperty(
		EntitySelector selector,
		List<OnHitEffect> effects,
		@Nullable Condition condition
) implements ConditionalProperty {
	public static final OpenIdentifier KEY_ID = new OpenIdentifier("minecraft", "on_hit");
	public static final ResolutionKey<List<OnHitProperty>> KEY = new ResolutionKey<>(KEY_ID);
	public static final PropertyKey<OnHitProperty> PROPERTY_KEY = new PropertyKey<>(OnHitProperty.class, KEY_ID.toString());

	public static Codec<OnHitProperty> codec(Codec<Condition> conditionCodec) {
		return RecordCodecBuilder.create(instance -> instance.group(
				EntitySelector.CODEC.fieldOf("selector").forGetter(OnHitProperty::selector),
				Codec.list(OnHitEffect.CODEC).fieldOf("effects").forGetter(OnHitProperty::effects),
				conditionCodec.optionalFieldOf("condition").forGetter(p -> Optional.ofNullable(p.condition()))
		).apply(instance, (selector, effects, condition) -> new OnHitProperty(selector, effects, condition.orElse(null))));
	}

	@Override
	public @Nullable Condition condition() {
		return condition;
	}

	public static class Engine extends AbstractConditionalPropertyEngine<OnHitProperty, List<OnHitProperty>> {
		public Engine() {
			super(KEY, PROPERTY_KEY);
		}

		@Override
		public List<OnHitProperty> apply(OptimizedBakedResult<OnHitProperty> baked) {
			return baked.all().collect(Collectors.toList());
		}
	}
}
