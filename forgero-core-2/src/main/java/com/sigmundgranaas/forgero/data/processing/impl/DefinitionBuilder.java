package com.sigmundgranaas.forgero.data.processing.impl;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.api.PropertyRegistry;
import com.sigmundgranaas.forgero.data.loading.api.data.PropertyData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.feature.FeatureData;
import com.sigmundgranaas.forgero.data.mapper.api.PropertyCodec;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * An internal, mutable builder used by the {@link DataProcessorImpl} during dependency resolution.
 * It accumulates tags and a generic properties map. It also handles the one-time consolidation
 * of dedicated 'attributes' and 'features' fields into the generic map.
 */
class DefinitionBuilder {
	private final Set<OpenIdentifier> tags = new LinkedHashSet<>();
	private final Map<String, List<PropertyData>> properties = new HashMap<>();
	private final List<PropertyCodec<?>> codecs;

	public DefinitionBuilder() {
		this.codecs = PropertyRegistry.getInstance().getPropertyCodecs();
	}

	public void mergeTags(@Nullable Collection<OpenIdentifier> otherTags) {
		if (otherTags != null) {
			this.tags.addAll(otherTags);
		}
	}

	public void mergeProperties(@Nullable Map<String, JsonElement> otherProperties) {
		if (otherProperties == null) {
			return;
		}
		for (PropertyCodec<?> codec : codecs) {
			String key = codec.getPropertyType();
			if (otherProperties.containsKey(key)) {
				JsonElement element = otherProperties.get(key);
				codec.getCodec().parse(com.mojang.serialization.JsonOps.INSTANCE, element)
						.resultOrPartial(System.err::println)
						.ifPresent(list -> {
							List<PropertyData> existing = this.properties.computeIfAbsent(key, k -> new ArrayList<>());
							// Special merging for attributes
							if (codec.getPropertyDataType().equals(AttributeData.class)) {
								Map<OpenIdentifier, PropertyData> mergedAttributes = new LinkedHashMap<>();
								existing.forEach(pd -> mergedAttributes.put(((AttributeData) pd).id(), pd));
								list.forEach(pd -> mergedAttributes.put(((AttributeData) pd).id(), pd));
								existing.clear();
								existing.addAll(mergedAttributes.values());
							} else {
								existing.addAll(list);
							}
						});
			}
		}
	}

	public void consolidateAttributes(@Nullable List<AttributeData> localAttributes) {
		if (localAttributes == null || localAttributes.isEmpty()) {
			return;
		}
		List<PropertyData> existing = this.properties.computeIfAbsent("forgero:attributes", k -> new ArrayList<>());
		Map<OpenIdentifier, PropertyData> mergedAttributes = new LinkedHashMap<>();
		existing.forEach(pd -> mergedAttributes.put(((AttributeData) pd).id(), pd));
		localAttributes.forEach(attr -> mergedAttributes.put(attr.id(), attr));
		existing.clear();
		existing.addAll(mergedAttributes.values());
	}

	public void consolidateFeatures(@Nullable List<FeatureData> localFeatures) {
		if (localFeatures == null || localFeatures.isEmpty()) {
			return;
		}
		List<PropertyData> existing = this.properties.computeIfAbsent("forgero:features", k -> new ArrayList<>());
		existing.addAll(localFeatures);
	}


	public void merge(MergedProperties other) {
		mergeTags(other.tags);

		if (other.properties != null) {
			other.properties.forEach((key, value) -> {
				List<PropertyData> existing = this.properties.computeIfAbsent(key, k -> new ArrayList<>());
				// Find codec for this property type
				codecs.stream()
						.filter(c -> c.getPropertyType().equals(key))
						.findFirst()
						.ifPresent(codec -> {
							if (codec.getPropertyDataType().equals(AttributeData.class)) {
								Map<OpenIdentifier, PropertyData> mergedAttributes = new LinkedHashMap<>();
								existing.forEach(pd -> mergedAttributes.put(((AttributeData) pd).id(), pd));
								value.forEach(pd -> mergedAttributes.put(((AttributeData) pd).id(), pd));
								existing.clear();
								existing.addAll(mergedAttributes.values());
							} else {
								// Default behavior: append
								existing.addAll(value);
							}
						});
			});
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
			@Nullable Map<String, List<PropertyData>> properties
	) {
	}
}
