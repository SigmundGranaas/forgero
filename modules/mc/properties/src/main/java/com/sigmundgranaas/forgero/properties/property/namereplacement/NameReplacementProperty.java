package com.sigmundgranaas.forgero.properties.property.namereplacement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.api.PropertyKey;
import com.sigmundgranaas.forgero.core.property.api.ResolutionKey;
import com.sigmundgranaas.forgero.core.property.api.custom.AbstractConditionalPropertyEngine;
import com.sigmundgranaas.forgero.core.property.api.custom.ConditionalProperty;
import com.sigmundgranaas.forgero.core.condition.api.Condition;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * A property that defines a name replacement. This is typically used for items that
 * change their display name based on certain conditions (e.g., if a specific part is present).
 */
public record NameReplacementProperty(
		String from,
		String to,
		@Nullable Condition condition
) implements ConditionalProperty {
	public static final OpenIdentifier KEY_ID = new OpenIdentifier("forgero", "name_replacement");
	public static final ResolutionKey<List<NameReplacementProperty>> KEY = new ResolutionKey<>(KEY_ID);
	public static final PropertyKey<NameReplacementProperty> PROPERTY_KEY = new PropertyKey<>(NameReplacementProperty.class, KEY_ID.toString());

	public static Codec<NameReplacementProperty> codec(Codec<Condition> conditionCodec) {
		return RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("from").forGetter(NameReplacementProperty::from),
				Codec.STRING.fieldOf("to").forGetter(NameReplacementProperty::to),
				conditionCodec.optionalFieldOf("condition").forGetter(p -> Optional.ofNullable(p.condition()))
		).apply(instance, (from, to, cond) -> new NameReplacementProperty(from, to, cond.orElse(null))));
	}

	/**
	 * The expert engine for resolving Name Replacement properties.
	 * <p>
	 * Final Result Type {@code <R>}: {@code List<NameReplacementProperty>}
	 */
	public static class Engine extends AbstractConditionalPropertyEngine<NameReplacementProperty> {
		public Engine() {
			super(KEY, PROPERTY_KEY);
		}
	}
}
