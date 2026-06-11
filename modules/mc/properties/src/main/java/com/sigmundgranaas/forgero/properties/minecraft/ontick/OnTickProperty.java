package com.sigmundgranaas.forgero.properties.minecraft.ontick;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.property.api.PropertyKey;
import com.sigmundgranaas.forgero.core.property.api.ResolutionKey;
import com.sigmundgranaas.forgero.core.property.api.custom.AbstractConditionalPropertyEngine;
import com.sigmundgranaas.forgero.core.property.api.custom.ConditionalProperty;
import com.sigmundgranaas.forgero.effects.entity.OnHitEffect;
import com.sigmundgranaas.forgero.properties.minecraft.entityselector.EntitySelector;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * The Orchestrator property for On-Tick events. It defines the "when" (periodically),
 * the "who" (via a selector with filters), and the "what" (via a list of effects).
 * This property is checked for any entity wearing or holding an item with it.
 *
 * <h3>JSON Configuration Example:</h3>
 * <pre>
 * {
 *   "selector": {
 *     "type": "forgero:aoe",
 *     "radius": 5,
 *     "filters": [
 *       { "type": "forgero:is_hostile" }
 *     ]
 *   },
 *   "effects": [
 *     {
 *       "type": "forgero:status_effect",
 *       "effect": "minecraft:slowness",
 *       "duration": 40,
 *       "amplifier": 0
 *     }
 *   ],
 *   "interval": 20,
 *   "condition": {
 *     "type": "forgero:is_sneaking"
 *   }
 * }
 * </pre>
 */
public record OnTickProperty(
		EntitySelector selector,
		List<OnHitEffect> effects,
		int interval,
		@Nullable Condition condition
) implements ConditionalProperty {
	public static final OpenIdentifier KEY_ID = new OpenIdentifier("minecraft", "on_tick");
	public static final ResolutionKey<List<OnTickProperty>> KEY = new ResolutionKey<>(KEY_ID);
	public static final PropertyKey<OnTickProperty> PROPERTY_KEY = new PropertyKey<>(OnTickProperty.class, KEY_ID.toString());

	public static Codec<OnTickProperty> codec(Codec<Condition> conditionCodec) {
		return RecordCodecBuilder.create(instance -> instance.group(
				EntitySelector.CODEC.fieldOf("selector").forGetter(OnTickProperty::selector),
				Codec.list(OnHitEffect.CODEC).fieldOf("effects").forGetter(OnTickProperty::effects),
				Codec.INT.optionalFieldOf("interval", 20).forGetter(OnTickProperty::interval),
				conditionCodec.optionalFieldOf("condition").forGetter(p -> Optional.ofNullable(p.condition()))
		).apply(instance, (selector, effects, interval, condition) -> new OnTickProperty(selector, effects, interval, condition.orElse(null))));
	}

	@Override
	public @Nullable Condition condition() {
		return condition;
	}

	public static class Engine extends AbstractConditionalPropertyEngine<OnTickProperty> {
		public Engine() {
			super(KEY, PROPERTY_KEY);
		}
	}
}
