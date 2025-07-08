package com.sigmundgranaas.forgero.data.processor;

import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.dto.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.dto.feature.FeatureData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An internal, mutable builder used by the {@link DataProcessorImpl} during dependency resolution.
 * It accumulates tags, attributes, and features from included definitions and the current definition,
 * handling overrides based on ID for attributes and by type for features.
 * This is *not* part of the public API between pipeline stages.
 */
class DefinitionBuilder {
	Set<OpenIdentifier> tags = new LinkedHashSet<>();
	Map<OpenIdentifier, AttributeData> attributes = new HashMap<>();
	Map<OpenIdentifier, FeatureData> features = new HashMap<>();

	public DefinitionBuilder mergeTags(@Nullable List<OpenIdentifier> otherTags) {
		if (otherTags != null) {
			this.tags.addAll(otherTags);
		}
		return this;
	}
	public DefinitionBuilder mergeTags(@Nullable Set<OpenIdentifier> otherTags) {
		if (otherTags != null) {
			this.tags.addAll(otherTags);
		}
		return this;
	}

	public DefinitionBuilder mergeAttributes(@Nullable List<AttributeData> otherAttributes) {
		if (otherAttributes != null) {
			otherAttributes.forEach(attr -> this.attributes.put(attr.id(), attr));
		}
		return this;
	}

	public DefinitionBuilder mergeFeatures(@Nullable List<FeatureData> otherFeatures) {
		if (otherFeatures != null) {
			otherFeatures.forEach(feature -> this.features.put(feature.type(), feature));
		}
		return this;
	}

	/**
	 * Merges properties from another DefinitionBuilder into this one.
	 *
	 * @param other The other builder to merge from.
	 * @return This builder for chaining.
	 */
	public DefinitionBuilder merge(@NotNull DefinitionBuilder other) {
		// Converting sets/maps to lists of values ensures that the mergeX methods' logic is consistently applied
		// (i.e., last one wins for attributes/features by ID/Type, tags are additive).
		mergeTags(new ArrayList<>(other.tags)); // Convert set to list for merging
		mergeAttributes(new ArrayList<>(other.attributes.values()));
		mergeFeatures(new ArrayList<>(other.features.values()));
		return this;
	}

	/**
	 * Converts the accumulated properties into immutable lists/sets.
	 *
	 * @return A DTO containing the finalized properties.
	 */
	public MergedProperties buildMergedProperties() {
		return new MergedProperties(
				tags.isEmpty() ? null : Collections.unmodifiableSet(tags),
				attributes.isEmpty() ? null : List.copyOf(attributes.values()),
				features.isEmpty() ? null : List.copyOf(features.values())
		);
	}

	/**
	 * A temporary record to hold the results of merging for internal use before
	 * creating the final Normalized... DTO.
	 */
	record MergedProperties(
			@Nullable Set<OpenIdentifier> tags,
			@Nullable List<AttributeData> attributes,
			@Nullable List<FeatureData> features
	) {}
}
