package com.sigmundgranaas.forgero.common.tooltip;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.property.api.PropertyKey;
import com.sigmundgranaas.forgero.core.property.api.ResolutionKey;
import com.sigmundgranaas.forgero.core.property.api.custom.AbstractConditionalPropertyEngine;
import com.sigmundgranaas.forgero.core.property.api.custom.ConditionalProperty;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A property that describes how something should appear in tooltips.
 * This is the fundamental building block of the tooltip system - it sits in the property map
 * and provides metadata about what to display and how.
 *
 * <p>Templates are translation keys resolved via {@code Text.translatable()}.
 * Use placeholders like {@code %s} in translation files for dynamic values.
 *
 * @param section   The section this descriptor belongs to (e.g., "forgero:description", "forgero:notes")
 * @param target    Optional ID of the property/resource this describes (null = standalone text)
 * @param template  Translation key with optional format placeholders
 * @param format    Display format hint
 * @param priority  Order within the section (lower = earlier)
 * @param metadata  Additional key-value pairs for section-specific behavior (e.g., "args" for translation params)
 * @param condition Optional condition for when this descriptor is active
 */
public record TooltipDescriptor(
		OpenIdentifier section,
		@Nullable OpenIdentifier target,
		String template,
		DisplayFormat format,
		int priority,
		Map<String, String> metadata,
		@Nullable Condition condition
) implements ConditionalProperty {

	public static final OpenIdentifier KEY_ID = OpenIdentifier.parse("forgero:tooltip_descriptors");
	public static final ResolutionKey<List<TooltipDescriptor>> KEY = new ResolutionKey<>(KEY_ID);
	public static final PropertyKey<TooltipDescriptor> PROPERTY_KEY =
			new PropertyKey<>(TooltipDescriptor.class, KEY_ID.toString());

	/**
	 * Display format hints for how values should be rendered.
	 */
	public enum DisplayFormat {
		/** Plain translatable text, no special formatting */
		TEXT,
		/** Numeric value with optional decimals */
		NUMBER,
		/** Value displayed as percentage (value * 100 with % suffix) */
		PERCENTAGE,
		/** Signed value with +/- prefix */
		ADDITIVE,
		/** Multiplier value with x prefix */
		MULTIPLIER,
		/** Show with comparison arrows/colors when comparison context is available */
		COMPARISON
	}

	/**
	 * Creates a simple text descriptor for a section.
	 */
	public TooltipDescriptor(OpenIdentifier section, String template) {
		this(section, null, template, DisplayFormat.TEXT, 0, Map.of(), null);
	}

	/**
	 * Creates a descriptor targeting another property.
	 */
	public TooltipDescriptor(OpenIdentifier section, OpenIdentifier target, String template, DisplayFormat format) {
		this(section, target, template, format, 0, Map.of(), null);
	}

	/**
	 * Creates a codec for TooltipDescriptor with the given condition codec.
	 */
	public static Codec<TooltipDescriptor> codec(Codec<Condition> conditionCodec) {
		return RecordCodecBuilder.create(instance -> instance.group(
				CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("section").forGetter(TooltipDescriptor::section),
				CodecConstants.OPEN_IDENTIFIER_CODEC.optionalFieldOf("target").forGetter(d -> Optional.ofNullable(d.target())),
				Codec.STRING.fieldOf("template").forGetter(TooltipDescriptor::template),
				Codec.STRING.optionalFieldOf("format", "TEXT")
						.xmap(DisplayFormat::valueOf, DisplayFormat::name)
						.forGetter(TooltipDescriptor::format),
				Codec.INT.optionalFieldOf("priority", 0).forGetter(TooltipDescriptor::priority),
				Codec.unboundedMap(Codec.STRING, Codec.STRING)
						.optionalFieldOf("metadata", Map.of())
						.forGetter(TooltipDescriptor::metadata),
				conditionCodec.optionalFieldOf("condition").forGetter(d -> Optional.ofNullable(d.condition()))
		).apply(instance, (section, target, template, format, priority, metadata, condition) ->
				new TooltipDescriptor(section, target.orElse(null), template, format, priority, metadata, condition.orElse(null))
		));
	}

	/**
	 * Gets the metadata value for a key, or empty if not present.
	 */
	public Optional<String> getMetadata(String key) {
		return Optional.ofNullable(metadata.get(key));
	}

	/**
	 * Gets the "args" metadata field split by comma, for translation arguments.
	 * e.g., "damage,speed" -> ["damage", "speed"]
	 */
	public List<String> getTranslationArgNames() {
		return getMetadata("args")
				.map(args -> List.of(args.split(",")))
				.orElse(List.of());
	}

	/**
	 * Checks if this descriptor has a specific target.
	 */
	public boolean hasTarget() {
		return target != null;
	}

	/**
	 * The engine for resolving TooltipDescriptor properties.
	 * <p>
	 * Final Result Type {@code <R>}: {@code List<TooltipDescriptor>}
	 */
	public static class Engine extends AbstractConditionalPropertyEngine<TooltipDescriptor> {
		public Engine() {
			super(KEY, PROPERTY_KEY);
		}
	}
}
