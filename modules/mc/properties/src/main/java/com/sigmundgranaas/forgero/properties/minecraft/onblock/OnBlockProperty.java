package com.sigmundgranaas.forgero.properties.minecraft.onblock;

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
 * Orchestrator property for shield-block / parry events: triggers when the wielder blocks an
 * attack with a shield. The defender (blocker) is the source, the attacker the initial target,
 * enabling parry counters (knockback, weakness, etc.).
 */
public record OnBlockProperty(
		EntitySelector selector,
		List<OnHitEffect> effects,
		@Nullable Condition condition
) implements ConditionalProperty {
	public static final OpenIdentifier KEY_ID = new OpenIdentifier("forgero", "on_block");
	public static final ResolutionKey<List<OnBlockProperty>> KEY = new ResolutionKey<>(KEY_ID);
	public static final PropertyKey<OnBlockProperty> PROPERTY_KEY = new PropertyKey<>(OnBlockProperty.class, KEY_ID.toString());

	public static Codec<OnBlockProperty> codec(Codec<Condition> conditionCodec) {
		return RecordCodecBuilder.create(instance -> instance.group(
				EntitySelector.CODEC.fieldOf("selector").forGetter(OnBlockProperty::selector),
				Codec.list(OnHitEffect.CODEC).fieldOf("effects").forGetter(OnBlockProperty::effects),
				conditionCodec.optionalFieldOf("condition").forGetter(p -> Optional.ofNullable(p.condition()))
		).apply(instance, (selector, effects, condition) -> new OnBlockProperty(selector, effects, condition.orElse(null))));
	}

	@Override
	public @Nullable Condition condition() {
		return condition;
	}

	public static class Engine extends AbstractConditionalPropertyEngine<OnBlockProperty> {
		public Engine() {
			super(KEY, PROPERTY_KEY);
		}
	}
}
