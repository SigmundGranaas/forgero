package com.sigmundgranaas.forgero.data.pipeline.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.api.RawDefinition;
import com.sigmundgranaas.forgero.data.loading.api.data.DefinitionData;

import java.util.*;

/**
 * Resolves the full `include` chain for a given definition.
 * It returns an ordered list of typed DTOs, with the most specific definition first
 * and the most generic (base) definition last. This order is ideal for merging properties.
 */
public class IncludeResolver {
	private final Map<OpenIdentifier, RawDefinition> rawDefinitions;
	private final Map<OpenIdentifier, List<DefinitionData>> cache = new HashMap<>();

	public IncludeResolver(Map<OpenIdentifier, RawDefinition> rawDefinitions) {
		this.rawDefinitions = rawDefinitions;
	}

	/**
	 * Resolves the include chain for a definition, returning all definitions
	 * in the chain from most specific to most generic.
	 *
	 * @param id The identifier of the definition to resolve
	 * @return An ordered list of definitions in the include chain
	 */
	public List<DefinitionData> resolve(OpenIdentifier id) {
		if (cache.containsKey(id)) {
			return cache.get(id);
		}
		List<DefinitionData> chain = new ArrayList<>();
		resolveRecursive(id, chain, new HashSet<>());
		cache.put(id, chain);
		return chain;
	}

	private void resolveRecursive(OpenIdentifier id, List<DefinitionData> chain, Set<OpenIdentifier> visited) {
		if (!visited.add(id)) {
			throw new IllegalStateException("Cyclic include dependency detected for ID: " + id);
		}

		RawDefinition rawDef = rawDefinitions.get(id);
		if (rawDef == null) {
			throw new IllegalArgumentException("Cannot resolve include: Definition not found for ID " + id);
		}
		DefinitionData dto = rawDef.data();
		chain.add(dto);

		// Use interface method directly - no instanceof checks needed
		List<OpenIdentifier> includes = dto.include();
		if (includes != null) {
			for (OpenIdentifier includeId : includes) {
				resolveRecursive(includeId, chain, visited);
			}
		}
		visited.remove(id);
	}
}
