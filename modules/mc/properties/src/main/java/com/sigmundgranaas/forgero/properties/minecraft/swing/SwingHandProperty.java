package com.sigmundgranaas.forgero.properties.minecraft.swing;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.property.api.PropertyKey;
import com.sigmundgranaas.forgero.core.property.api.ResolutionKey;
import com.sigmundgranaas.forgero.core.property.api.custom.AbstractConditionalPropertyEngine;
import com.sigmundgranaas.forgero.core.property.api.custom.ConditionalProperty;
import com.sigmundgranaas.forgero.effects.entity.SwingEffect;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * The Orchestrator property for Swing Hand events. It defines the "when" (on hand swing),
 * and the "what" (via a list of effects).
 *
 * <p>Swing events trigger when a player swings their hand (attack animation),
 * regardless of whether they hit something.</p>
 *
 * <h3>JSON Configuration Example:</h3>
 * <pre>
 * {
 *   "effects": [
 *     { "type": "forgero:swing_sound", "sound": "minecraft:entity.player.attack.sweep" },
 *     { "type": "forgero:swing_particle", "particle": "minecraft:sweep_attack", "count": 3 }
 *   ],
 *   "condition": {
 *     "type": "forgero:is_sneaking"
 *   }
 * }
 * </pre>
 */
public record SwingHandProperty(
		List<SwingEffect> effects,
		@Nullable Condition condition
) implements ConditionalProperty {
	public static final OpenIdentifier KEY_ID = new OpenIdentifier("minecraft", "on_swing");
	public static final ResolutionKey<List<SwingHandProperty>> KEY = new ResolutionKey<>(KEY_ID);
	public static final PropertyKey<SwingHandProperty> PROPERTY_KEY = new PropertyKey<>(SwingHandProperty.class, KEY_ID.toString());

	public static Codec<SwingHandProperty> codec(Codec<Condition> conditionCodec) {
		return RecordCodecBuilder.create(instance -> instance.group(
				Codec.list(SwingEffect.CODEC).fieldOf("effects").forGetter(SwingHandProperty::effects),
				conditionCodec.optionalFieldOf("condition").forGetter(p -> Optional.ofNullable(p.condition()))
		).apply(instance, (effects, condition) -> new SwingHandProperty(effects, condition.orElse(null))));
	}

	@Override
	public @Nullable Condition condition() {
		return condition;
	}

	public static class Engine extends AbstractConditionalPropertyEngine<SwingHandProperty> {
		public Engine() {
			super(KEY, PROPERTY_KEY);
		}
	}
}
