package com.sigmundgranaas.forgero.properties.minecraft.oncrit;

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
 * Orchestrator property for critical-hit events: triggers when a player lands a vanilla critical
 * hit on a living entity. The player is the source, the victim the initial target.
 */
public record OnCritProperty(
		EntitySelector selector,
		List<OnHitEffect> effects,
		@Nullable Condition condition
) implements ConditionalProperty {
	public static final OpenIdentifier KEY_ID = new OpenIdentifier("forgero", "on_crit");
	public static final ResolutionKey<List<OnCritProperty>> KEY = new ResolutionKey<>(KEY_ID);
	public static final PropertyKey<OnCritProperty> PROPERTY_KEY = new PropertyKey<>(OnCritProperty.class, KEY_ID.toString());

	public static Codec<OnCritProperty> codec(Codec<Condition> conditionCodec) {
		return RecordCodecBuilder.create(instance -> instance.group(
				EntitySelector.CODEC.fieldOf("selector").forGetter(OnCritProperty::selector),
				Codec.list(OnHitEffect.CODEC).fieldOf("effects").forGetter(OnCritProperty::effects),
				conditionCodec.optionalFieldOf("condition").forGetter(p -> Optional.ofNullable(p.condition()))
		).apply(instance, (selector, effects, condition) -> new OnCritProperty(selector, effects, condition.orElse(null))));
	}

	@Override
	public @Nullable Condition condition() {
		return condition;
	}

	public static class Engine extends AbstractConditionalPropertyEngine<OnCritProperty> {
		public Engine() {
			super(KEY, PROPERTY_KEY);
		}
	}
}
