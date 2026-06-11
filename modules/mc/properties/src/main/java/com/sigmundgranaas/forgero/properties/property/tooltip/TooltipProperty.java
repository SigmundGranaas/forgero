package com.sigmundgranaas.forgero.properties.property.tooltip;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.api.PropertyKey;
import com.sigmundgranaas.forgero.core.property.api.ResolutionKey;
import com.sigmundgranaas.forgero.core.property.api.custom.AbstractConditionalPropertyEngine;
import com.sigmundgranaas.forgero.core.property.api.custom.ConditionalProperty;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Represents a single entry for a custom tooltip section, e.g., "ingredient_count".
 * These entries are designed to be collected and displayed together.
 */
public record TooltipProperty(
		OpenIdentifier key,
		String value,
		String format,
		@Nullable Condition condition
) implements ConditionalProperty {

	public static final OpenIdentifier KEY_ID = OpenIdentifier.parse("forgero:tooltip_sections");
	public static final ResolutionKey<List<TooltipProperty>> KEY = new ResolutionKey<>(KEY_ID);
	public static final PropertyKey<TooltipProperty> PROPERTY_KEY = new PropertyKey<>(TooltipProperty.class, KEY_ID.toString());

	public static Codec<TooltipProperty> codec(Codec<Condition> conditionCodec) {
		return RecordCodecBuilder.create(instance -> instance.group(
				CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("key").forGetter(TooltipProperty::key),
				Codec.STRING.fieldOf("value").forGetter(TooltipProperty::value),
				Codec.STRING.optionalFieldOf("format", "TEXT").forGetter(TooltipProperty::format),
				conditionCodec.optionalFieldOf("condition").forGetter(p -> Optional.ofNullable(p.condition))
		).apply(instance, (key, value, format, cond) -> new TooltipProperty(key, value, format, cond.orElse(null))));
	}

	/**
	 * The engine for resolving Tooltip properties.
	 * <p>
	 * Final Result Type {@code <R>}: {@code List<TooltipProperty>}
	 */
	public static class Engine extends AbstractConditionalPropertyEngine<TooltipProperty> {
		public Engine() {
			super(KEY, PROPERTY_KEY);
		}
	}
}
