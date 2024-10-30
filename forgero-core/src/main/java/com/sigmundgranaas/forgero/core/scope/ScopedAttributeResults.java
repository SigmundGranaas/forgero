package com.sigmundgranaas.forgero.core.scope;

import com.sigmundgranaas.forgero.core.attribute.AttributeComponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class ScopedAttributeResults {
	private final List<AttributeComponent> resolvedResults;
	private final Map<String, List<ScopedAttributeComponent>> unresolvedResults;
	private final Set<String> potentialInvalidations;

	private ScopedAttributeResults(
			List<AttributeComponent> resolvedResults,
			Map<String, List<ScopedAttributeComponent>> unresolvedResults,
			Set<String> potentialInvalidations) {
		this.resolvedResults = List.copyOf(resolvedResults);
		this.unresolvedResults = Collections.unmodifiableMap(
				unresolvedResults.entrySet().stream()
						.collect(Collectors.toMap(
								Map.Entry::getKey,
								e -> List.copyOf(e.getValue())
						))
		);
		this.potentialInvalidations = Set.copyOf(potentialInvalidations);
	}

	public static ScopedAttributeResults empty() {
		return new ScopedAttributeResults(List.of(), Map.of(), Set.of());
	}

	public List<AttributeComponent> results() {
		return resolvedResults;
	}

	public Map<String, List<ScopedAttributeComponent>> unresolvedResults() {
		return unresolvedResults;
	}

	public Set<String> potentialInvalidations() {
		return potentialInvalidations;
	}

	public ScopedAttributeResults resolve(Scope scope) {
		List<ScopedAttributeComponent> componentsToResolve = unresolvedResults.getOrDefault(scope.identifier(), List.of());
		if (componentsToResolve.isEmpty()) {
			return this;
		}

		List<AttributeComponent> newResolved = new ArrayList<>(resolvedResults);
		List<ScopedAttributeComponent> remainingUnresolved = new ArrayList<>();

		for (ScopedAttributeComponent component : componentsToResolve) {
			ScopeEvaluator.EvaluationResult result = component.evaluate(scope);
			switch (result.status()) {
				case VALID -> newResolved.add(component.attributeComponent());
				case PARTIAL -> remainingUnresolved.add(
						new ScopedAttributeComponent(component.attributeComponent(), result.remainingEvaluator())
				);
				case INVALID -> {}  // Skip invalid components
			}
		}

		Map<String, List<ScopedAttributeComponent>> newUnresolved = new HashMap<>(unresolvedResults);
		if (remainingUnresolved.isEmpty()) {
			newUnresolved.remove(scope.identifier());
		} else {
			newUnresolved.put(scope.identifier(), remainingUnresolved);
		}

		return new ScopedAttributeResults(newResolved, newUnresolved, potentialInvalidations);
	}

	public ScopedAttributeResults addResolved(Collection<AttributeComponent> attributes) {
		if (attributes == null || attributes.isEmpty()) {
			return this;
		}

		List<AttributeComponent> newResolved = new ArrayList<>(resolvedResults);
		newResolved.addAll(attributes);

		return new ScopedAttributeResults(newResolved, unresolvedResults, potentialInvalidations);
	}

	public ScopedAttributeResults addUnresolved(String scopeIdentifier, Collection<ScopedAttributeComponent> components) {
		if (components == null || components.isEmpty() || scopeIdentifier == null) {
			return this;
		}

		// Filter out components that don't match the scope identifier
		List<ScopedAttributeComponent> validComponents = components.stream()
				.filter(component -> component.matches(Scope.simple(scopeIdentifier)))
				.toList();

		if (validComponents.isEmpty()) {
			return this;
		}

		Map<String, List<ScopedAttributeComponent>> newUnresolved = new HashMap<>(unresolvedResults);
		List<ScopedAttributeComponent> existing = new ArrayList<>(
				newUnresolved.getOrDefault(scopeIdentifier, List.of())
		);
		existing.addAll(validComponents);
		newUnresolved.put(scopeIdentifier, existing);

		return new ScopedAttributeResults(resolvedResults, newUnresolved, potentialInvalidations);
	}

	public ScopedAttributeResults merge(ScopedAttributeResults other) {
		if (other == null) {
			return this;
		}

		List<AttributeComponent> newResolved = new ArrayList<>(resolvedResults);
		newResolved.addAll(other.results());

		Map<String, List<ScopedAttributeComponent>> newUnresolved = new HashMap<>(unresolvedResults);
		other.unresolvedResults().forEach((scope, components) -> {
			List<ScopedAttributeComponent> existing = new ArrayList<>(
					newUnresolved.getOrDefault(scope, List.of())
			);
			existing.addAll(components);
			newUnresolved.put(scope, existing);
		});

		Set<String> newInvalidations = new HashSet<>(potentialInvalidations);
		newInvalidations.addAll(other.potentialInvalidations());

		return new ScopedAttributeResults(newResolved, newUnresolved, newInvalidations);
	}

	public boolean hasUnresolvedAttributesFor(String scopeIdentifier) {
		return unresolvedResults.containsKey(scopeIdentifier) &&
				!unresolvedResults.get(scopeIdentifier).isEmpty();
	}

	public boolean wouldResolveAny(Scope scope) {
		List<ScopedAttributeComponent> components = unresolvedResults.get(scope.identifier());
		if (components == null || components.isEmpty()) {
			return false;
		}
		return components.stream().anyMatch(component -> component.matches(scope));
	}
}

