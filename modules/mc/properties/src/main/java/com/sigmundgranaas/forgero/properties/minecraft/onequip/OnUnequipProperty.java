package com.sigmundgranaas.forgero.properties.minecraft.onequip;

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
 * Orchestrator property fired when the item is removed from an armor slot. Effects apply to the
 * former wearer (source = target), e.g. to strip a set-bonus status effect.
 */
public record OnUnequipProperty(
		EntitySelector selector,
		List<OnHitEffect> effects,
		@Nullable Condition condition
) implements ConditionalProperty {
	public static final OpenIdentifier KEY_ID = new OpenIdentifier("forgero", "on_unequip");
	public static final ResolutionKey<List<OnUnequipProperty>> KEY = new ResolutionKey<>(KEY_ID);
	public static final PropertyKey<OnUnequipProperty> PROPERTY_KEY = new PropertyKey<>(OnUnequipProperty.class, KEY_ID.toString());

	public static Codec<OnUnequipProperty> codec(Codec<Condition> conditionCodec) {
		return RecordCodecBuilder.create(instance -> instance.group(
				EntitySelector.CODEC.fieldOf("selector").forGetter(OnUnequipProperty::selector),
				Codec.list(OnHitEffect.CODEC).fieldOf("effects").forGetter(OnUnequipProperty::effects),
				conditionCodec.optionalFieldOf("condition").forGetter(p -> Optional.ofNullable(p.condition()))
		).apply(instance, (selector, effects, condition) -> new OnUnequipProperty(selector, effects, condition.orElse(null))));
	}

	@Override
	public @Nullable Condition condition() {
		return condition;
	}

	public static class Engine extends AbstractConditionalPropertyEngine<OnUnequipProperty> {
		public Engine() {
			super(KEY, PROPERTY_KEY);
		}
	}
}
