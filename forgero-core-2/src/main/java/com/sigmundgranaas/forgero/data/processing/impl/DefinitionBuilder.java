package com.sigmundgranaas.forgero.data.processing.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.feature.FeatureData;
import com.sigmundgranaas.forgero.data.loading.impl.codec.AttributeCodecs;
import com.sigmundgranaas.forgero.data.loading.impl.codec.FeatureCodecs;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * An internal, mutable builder used by the {@link DataProcessorImpl} during dependency resolution.
 * It accumulates tags and a generic properties map. It also handles the one-time consolidation
 * of dedicated 'attributes' and 'features' fields into the generic map.
 */
class DefinitionBuilder {
	private final Set<OpenIdentifier> tags = new LinkedHashSet<>();
	private final Map<String, JsonElement> properties = new HashMap<>();

	public void mergeTags(@Nullable Collection<OpenIdentifier> otherTags) {
		if (otherTags != null) {
			this.tags.addAll(otherTags);
		}
	}

	public void mergeProperties(@Nullable Map<String, JsonElement> otherProperties) {
		if (otherProperties != null) {
			// Don't merge attributes/features directly, they need special handling
			otherProperties.forEach((key, value) -> {
				if (!key.equals("forgero:attributes") && !key.equals("forgero:features")) {
					this.properties.put(key, value);
				}
			});
		}
	}

	public void consolidateAttributes(@Nullable List<AttributeData> localAttributes) {
		// This method will now merge attributes from local and existing properties
		Map<OpenIdentifier, AttributeData> mergedAttributes = new LinkedHashMap<>();

		// 1. Get inherited attributes from properties map
		if (this.properties.containsKey("forgero:attributes")) {
			JsonElement existingJson = this.properties.get("forgero:attributes");
			var existingList = AttributeCodecs.ATTRIBUTE_DATA_LIST_CODEC.parse(JsonOps.INSTANCE, existingJson).result().orElse(List.of());
			existingList.forEach(attr -> mergedAttributes.put(attr.id(), attr));
		}

		// 2. Add/overwrite with local attributes
		if (localAttributes != null) {
			localAttributes.forEach(attr -> mergedAttributes.put(attr.id(), attr));
		}

		// 3. Re-encode back into the properties map
		if (!mergedAttributes.isEmpty()) {
			JsonArray array = new JsonArray();
			mergedAttributes.values().forEach(attr ->
					AttributeCodecs.ATTRIBUTE_DATA_CODEC.encodeStart(JsonOps.INSTANCE, attr).result().ifPresent(array::add)
			);
			this.properties.put("forgero:attributes", array);
		} else {
			this.properties.remove("forgero:attributes");
		}
	}

	public void consolidateFeatures(@Nullable List<FeatureData> localFeatures) {
		// This method will now merge features from local and existing properties
		List<FeatureData> combinedFeatures = new ArrayList<>();

		// 1. Get inherited features
		if (this.properties.containsKey("forgero:features")) {
			JsonElement existingJson = this.properties.get("forgero:features");
			var existingList = FeatureCodecs.FEATURE_DATA_LIST_CODEC.parse(JsonOps.INSTANCE, existingJson).result().orElse(List.of());
			combinedFeatures.addAll(existingList);
		}

		// 2. Add local features
		if (localFeatures != null) {
			combinedFeatures.addAll(localFeatures);
		}


		// 3. Re-encode back into the properties map
		if (!combinedFeatures.isEmpty()) {
			JsonArray array = new JsonArray();
			combinedFeatures.forEach(feat ->
					FeatureCodecs.FEATURE_DATA_CODEC.encodeStart(JsonOps.INSTANCE, feat).result().ifPresent(array::add)
			);
			this.properties.put("forgero:features", array);
		} else {
			this.properties.remove("forgero:features");
		}
	}


	public void merge(MergedProperties other) {
		mergeTags(other.tags);

		// Handle merging properties, including special logic for attributes/features
		if (other.properties != null) {
			// Merge general properties
			other.properties.forEach((key, value) -> {
				if (!key.equals("forgero:attributes") && !key.equals("forgero:features")) {
					this.properties.put(key, value);
				}
			});
			// Merge attributes
			if (other.properties.containsKey("forgero:attributes")) {
				var otherAttrs = AttributeCodecs.ATTRIBUTE_DATA_LIST_CODEC.parse(JsonOps.INSTANCE, other.properties.get("forgero:attributes")).result().orElse(List.of());
				consolidateAttributes(otherAttrs);
			}
			// Merge features
			if (other.properties.containsKey("forgero:features")) {
				var otherFeatures = FeatureCodecs.FEATURE_DATA_LIST_CODEC.parse(JsonOps.INSTANCE, other.properties.get("forgero:features")).result().orElse(List.of());
				consolidateFeatures(otherFeatures);
			}
		}
	}

	public MergedProperties buildMergedProperties() {
		return new MergedProperties(
				tags.isEmpty() ? null : Collections.unmodifiableSet(tags),
				properties.isEmpty() ? null : Collections.unmodifiableMap(properties)
		);
	}

	record MergedProperties(
			@Nullable Set<OpenIdentifier> tags,
			@Nullable Map<String, JsonElement> properties
	) {
	}
}
