package com.sigmundgranaas.forgero.core.state.identity;

import static com.sigmundgranaas.forgero.core.identifier.Common.ELEMENT_SEPARATOR;

import java.util.List;
import java.util.stream.Collectors;

import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.identity.sorting.SortingRuleRegistry;

public class NameCompositor {
	private ModificationRuleRegistry registry;
	private final SortingRuleRegistry sorting;

	public NameCompositor(ModificationRuleRegistry registry, SortingRuleRegistry sorting) {
		this.registry = registry;
		this.sorting = sorting;
	}

	public static NameCompositor of() {
		return new NameCompositor(ModificationRuleRegistry.staticRegistry(), SortingRuleRegistry.staticRegistry());
	}

	public String compositeName(List<State> parts) {
		return parts.stream()
				.sorted(sorting.getComparator())
				.map(registry::applyRules)
				.filter(element -> !element.isEmpty())
				.collect(Collectors.joining(ELEMENT_SEPARATOR));
	}
}
