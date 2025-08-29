package com.sigmundgranaas.forgero.data.pipeline.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.api.RawDefinition;
import com.sigmundgranaas.forgero.data.loading.api.data.*;
import com.sigmundgranaas.forgero.data.loading.api.data.template.EquipmentTemplateData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.PartTemplateData;

import java.util.*;

/**
 * Resolves the full `include` chain for a given definition.
 * It returns an ordered list of raw DTOs, with the most specific definition first
 * and the most generic (base) definition last. This order is ideal for merging properties.
 */
public class IncludeResolver {
	private final Map<OpenIdentifier, RawDefinition> rawDefinitions;
	private final Map<OpenIdentifier, List<Object>> cache = new HashMap<>();

	public IncludeResolver(Map<OpenIdentifier, RawDefinition> rawDefinitions) {
		this.rawDefinitions = rawDefinitions;
	}

	public List<Object> resolve(OpenIdentifier id) {
		if (cache.containsKey(id)) {
			return cache.get(id);
		}
		List<Object> chain = new ArrayList<>();
		resolveRecursive(id, chain, new HashSet<>());
		cache.put(id, chain);
		return chain;
	}

	private void resolveRecursive(OpenIdentifier id, List<Object> chain, Set<OpenIdentifier> visited) {
		if (!visited.add(id)) {
			throw new IllegalStateException("Cyclic include dependency detected for ID: " + id);
		}

		RawDefinition rawDef = rawDefinitions.get(id);
		if (rawDef == null) {
			throw new IllegalArgumentException("Cannot resolve include: Definition not found for ID " + id);
		}
		Object dto = rawDef.data();
		chain.add(dto);

		List<OpenIdentifier> includes = getIncludes(dto);
		if (includes != null) {
			for (OpenIdentifier includeId : includes) {
				resolveRecursive(includeId, chain, visited);
			}
		}
		visited.remove(id);
	}

	private List<OpenIdentifier> getIncludes(Object dto) {
		if (dto instanceof MaterialData data) return data.include();
		if (dto instanceof ShapeData data) return data.include();
		if (dto instanceof SchematicData data) return data.include();
		if (dto instanceof StaticData data) return data.include();
		if (dto instanceof PartTemplateData data) return data.include();
		if (dto instanceof EquipmentTemplateData data) return data.include();
		return Collections.emptyList();
	}
}
