package com.sigmundgranaas.forgero.loader.property.namereplacement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.api.PropertyKey;
import com.sigmundgranaas.forgero.core.property.api.ResolutionKey;
import com.sigmundgranaas.forgero.core.property.api.custom.AbstractConditionalPropertyEngine;
import com.sigmundgranaas.forgero.core.property.api.custom.ConditionalProperty;
import com.sigmundgranaas.forgero.core.property.api.custom.OptimizedBakedResult;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;

import javax.annotation.Nullable;
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
	public static final ResolutionKey<Optional<String>> KEY = new ResolutionKey<>(KEY_ID);
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
	 * Intermediate Baked Type {@code <B>}: {@link OptimizedBakedResult}
	 * Final Result Type {@code <R>}: {@code Optional<String>}
	 */
	public static class Engine extends AbstractConditionalPropertyEngine<NameReplacementProperty, Optional<String>> {
		public Engine() {
			super(KEY, PROPERTY_KEY);
		}

		@Override
		public Optional<String> apply(OptimizedBakedResult<NameReplacementProperty> baked, DynamicContext context) {
			return baked.stream(context)
					.map(NameReplacementProperty::to)
					.findFirst(); // Only the first valid replacement wins
		}
	}
}
