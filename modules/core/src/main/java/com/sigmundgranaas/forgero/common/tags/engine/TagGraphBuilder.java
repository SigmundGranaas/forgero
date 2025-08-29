package com.sigmundgranaas.forgero.common.tags.engine;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TagGraphBuilder {
	private final Map<OpenIdentifier, Set<OpenIdentifier>> parentRelationships = new ConcurrentHashMap<>();

	public TagGraphBuilder add(OpenIdentifier id, Collection<OpenIdentifier> parents) {
		parentRelationships.computeIfAbsent(id, k -> new HashSet<>()).addAll(parents);
		return this;
	}

	public TagGraph build() {
		validateForCycles();
		return new TagGraph(parentRelationships);
	}

	private void validateForCycles() {
		Set<OpenIdentifier> visited = new HashSet<>();
		Set<OpenIdentifier> recursionStack = new HashSet<>();

		for (OpenIdentifier id : parentRelationships.keySet()) {
			if (!visited.contains(id)) {
				detectCycleUtil(id, visited, recursionStack);
			}
		}
	}

	private void detectCycleUtil(OpenIdentifier currentId, Set<OpenIdentifier> visited, Set<OpenIdentifier> recursionStack) {
		recursionStack.add(currentId);

		for (OpenIdentifier parent : parentRelationships.getOrDefault(currentId, Collections.emptySet())) {
			if (recursionStack.contains(parent)) {
				throw new IllegalStateException("Cycle detected in tag hierarchy involving: " + parent.toString() + " and " + currentId.toString());
			}

			if (!visited.contains(parent)) {
				detectCycleUtil(parent, visited, recursionStack);
			}
		}
		recursionStack.remove(currentId);
		visited.add(currentId);
	}
}
