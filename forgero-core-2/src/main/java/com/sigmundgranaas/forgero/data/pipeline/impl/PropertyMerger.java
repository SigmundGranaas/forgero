package com.sigmundgranaas.forgero.data.pipeline.impl;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.CompositeAttributeComponent;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.data.loading.api.data.*;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.HostData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.EquipmentTemplateData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.PartTemplateData;
import com.sigmundgranaas.forgero.data.loading.impl.codec.OperatorMapper;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Merges properties from a chain of raw DTOs into a single result.
 * It also parses the raw JsonElements into their final runtime Java types using injected codecs.
 */
public class PropertyMerger {
	private final Map<String, Codec<? extends List<?>>> propertyCodecs;
	private final OperatorMapper operatorMapper = new OperatorMapper();

	public record MergedResult(Set<OpenIdentifier> tags, Map<String, List<?>> properties, @Nullable HostData host) {
	}

	public PropertyMerger(Map<String, Codec<? extends List<?>>> propertyCodecs) {
		this.propertyCodecs = propertyCodecs;
	}

	public MergedResult merge(List<Object> dtoList) {
		Set<OpenIdentifier> mergedTags = new LinkedHashSet<>();
		Map<String, JsonElement> mergedJsonProperties = new HashMap<>();
		List<AttributeData> mergedAttributes = new ArrayList<>();
		HostData hostData = null;

		// Check if this merge is for a template-based component
		boolean isTemplateBasedMerge = dtoList.stream().anyMatch(dto -> dto instanceof PartTemplateData || dto instanceof EquipmentTemplateData);

		// Iterate in reverse to ensure that more specific DTOs override generic ones.
		for (int i = dtoList.size() - 1; i >= 0; i--) {
			Object dto = dtoList.get(i);

			// Apply tag inheritance logic
			if (getTags(dto) != null) {
				if (isTemplateBasedMerge) {
					// If it's a template merge, only inherit tags from non-material/non-shape DTOs (i.e., from the template itself)
					if (!(dto instanceof MaterialData) && !(dto instanceof ShapeData)) {
						mergedTags.addAll(getTags(dto));
					}
				} else {
					// If it's not a template merge (e.g., a static material), add its own tags
					mergedTags.addAll(getTags(dto));
				}
			}

			if (getAttributes(dto) != null && !isTemplateBasedMerge) {
				mergedAttributes.addAll(getAttributes(dto));
			}

			if (getProperties(dto) != null && !isTemplateBasedMerge) {
				mergedJsonProperties.putAll(getProperties(dto));
			}
			if (getHost(dto) != null) {
				hostData = getHost(dto);
			}
		}

		Map<String, List<?>> parsedProperties = new HashMap<>();
		mergedJsonProperties.forEach((key, json) -> {
			Codec<? extends List<?>> codec = propertyCodecs.get(key);
			if (codec != null) {
				codec.parse(JsonOps.INSTANCE, json)
						.resultOrPartial(System.err::println)
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
		var id = Optional.ofNullable(data.id()).map(OpenIdentifier::toString);

		if (data.composite() != null) {
			return new CompositeAttributeComponent(
					id,
					data.type(),
					computation.value(),
					operator,
					order,
					data.composite()
			);
		} else {
			return new SimpleAttribute(
					id,
					data.type(),
					computation.value(),
					operator,
					order,
					data.condition()
			);
		}
	}

	private List<OpenIdentifier> getTags(Object dto) {
		if (dto instanceof MaterialData data) return data.tags();
		if (dto instanceof ShapeData data) return data.tags();
		if (dto instanceof SchematicData data) return data.tags();
		if (dto instanceof StaticData data) return data.tags();
		if (dto instanceof PartTemplateData data) return data.tags();
		if (dto instanceof EquipmentTemplateData data) return data.tags();
		return Collections.emptyList();
	}

	private List<AttributeData> getAttributes(Object dto) {
		if (dto instanceof MaterialData data) return data.attributes();
		if (dto instanceof ShapeData data) return data.attributes();
		if (dto instanceof StaticData data) return data.attributes();
		if (dto instanceof PartTemplateData data) return data.attributes();
		if (dto instanceof EquipmentTemplateData data) return data.attributes();
		return Collections.emptyList();
	}

	private Map<String, JsonElement> getProperties(Object dto) {
		if (dto instanceof MaterialData data) return data.properties();
		if (dto instanceof ShapeData data) return data.properties();
		if (dto instanceof SchematicData data) return data.properties();
		if (dto instanceof StaticData data) return data.properties();
		if (dto instanceof PartTemplateData data) return data.properties();
		if (dto instanceof EquipmentTemplateData data) return data.properties();
		return Collections.emptyMap();
	}

	private HostData getHost(Object dto) {
		if (dto instanceof MaterialData data) return data.host();
		if (dto instanceof ShapeData data) return data.host();
		if (dto instanceof SchematicData data) return data.host();
		if (dto instanceof StaticData data) return data.host();
		return null;
	}
}
