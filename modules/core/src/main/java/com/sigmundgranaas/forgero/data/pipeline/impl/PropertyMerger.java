package com.sigmundgranaas.forgero.data.pipeline.impl;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.property.api.PropertyKey;
import com.sigmundgranaas.forgero.data.loading.api.data.DefinitionData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.HostData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.TemplateData;
import com.sigmundgranaas.forgero.data.loading.impl.codec.OperatorMapper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Merges properties from a chain of raw DTOs into a single result.
 * It also parses the raw JsonElements into their final runtime Java types using injected codecs.
 */
public class PropertyMerger {
	private static final Logger LOGGER = LoggerFactory.getLogger(PropertyMerger.class);
	private final Map<String, Codec<? extends List<?>>> propertyCodecs;
	private final OperatorMapper operatorMapper = new OperatorMapper();

	public record MergedResult(Set<OpenIdentifier> tags, Map<String, List<?>> properties, @Nullable HostData host) {
	}

	public PropertyMerger(Map<PropertyKey<?>, Codec<? extends List<?>>> propertyCodecs) {
		this.propertyCodecs = propertyCodecs.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().key(), entry -> entry.getValue()));
	}

	public MergedResult merge(List<DefinitionData> dtoList) {
		Set<OpenIdentifier> mergedTags = new LinkedHashSet<>();
		Map<String, JsonElement> mergedJsonProperties = new HashMap<>();
		List<AttributeData> mergedAttributes = new ArrayList<>();
		HostData hostData = null;

		// Check if this merge is for a template-based component
		boolean isTemplateBasedMerge = dtoList.stream().anyMatch(dto -> dto instanceof TemplateData);

		// Iterate in reverse to ensure that more specific DTOs override generic ones.
		for (int i = dtoList.size() - 1; i >= 0; i--) {
			DefinitionData dto = dtoList.get(i);
			boolean isLastDto = (i == dtoList.size() - 1);

			// Apply tag inheritance logic - use interface method directly
			if (dto.tags() != null) {
				if (isTemplateBasedMerge) {
					// If it's a template merge, only inherit tags from templates (not materials/shapes)
					if (dto instanceof TemplateData) {
						mergedTags.addAll(dto.tags());
					}
				} else {
					// If it's not a template merge (e.g., a static material), add its own tags
					mergedTags.addAll(dto.tags());
				}
			}

			// Add local_tags ONLY from the final DTO (not inherited)
			if (isLastDto && dto.localTags() != null) {
				mergedTags.addAll(dto.localTags());
			}

			// Handle attributes based on merge type
			if (dto.attributes() != null) {
				if (isTemplateBasedMerge) {
					// For template-based merges, only include the TEMPLATE's own attributes.
					// Material/shape attributes stay on their respective components in the structure.
					// This allows context-based composition to work correctly.
					if (dto instanceof TemplateData) {
						mergedAttributes.addAll(dto.attributes());
					}
				} else {
					// For non-template merges (e.g., includes), merge all attributes
					mergedAttributes.addAll(dto.attributes());
				}
			}

			// Add local_attributes ONLY from the final DTO (not inherited)
			if (isLastDto && dto.localAttributes() != null) {
				if (isTemplateBasedMerge) {
					if (dto instanceof TemplateData) {
						mergedAttributes.addAll(dto.localAttributes());
					}
				} else {
					mergedAttributes.addAll(dto.localAttributes());
				}
			}

			// Handle properties similarly
			if (dto.properties() != null) {
				if (isTemplateBasedMerge) {
					if (dto instanceof TemplateData) {
						mergedJsonProperties.putAll(dto.properties());
					}
				} else {
					mergedJsonProperties.putAll(dto.properties());
				}
			}
			if (dto.host() != null) {
				hostData = dto.host();
			}
		}

		Map<String, List<?>> parsedProperties = new HashMap<>();
		mergedJsonProperties.forEach((key, json) -> {
			Codec<? extends List<?>> codec = propertyCodecs.get(key);
			if (codec != null) {
				codec.parse(JsonOps.INSTANCE, json)
						.resultOrPartial(error -> LOGGER.warn("Failed to parse property '{}': {}", key, error))
						.ifPresent(list -> parsedProperties.put(key, list));
			}
		});

		if (!mergedAttributes.isEmpty()) {
			List<Attribute> runtimeAttributes = mergedAttributes.stream()
					.map(this::convertAttributeData)
					.collect(Collectors.toList());
			parsedProperties.put(Attribute.KEY.key(), runtimeAttributes);
		}


		return new MergedResult(mergedTags, parsedProperties, hostData);
	}

	private Attribute convertAttributeData(AttributeData data) {
		var computation = data.computation();
		var operator = operatorMapper.apply(computation.operator());
		var order = operatorMapper.leveledOrder(computation.order());

		return new SimpleAttribute(
				data.id(),
				data.type(),
				computation.value(),
				operator,
				order,
				data.context(),
				data.condition()
		);
	}
}
