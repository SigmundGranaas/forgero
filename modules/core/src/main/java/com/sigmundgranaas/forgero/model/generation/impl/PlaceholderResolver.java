package com.sigmundgranaas.forgero.model.generation.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class PlaceholderResolver {
	private static final Pattern PATTERN = Pattern.compile("\\{([^}]+)}");

	public String resolve(String template, Map<String, Component> context) {
		if (template == null || template.isEmpty()) {
			return "";
		}
		// Use replaceAll with a lambda for cleaner logic
		return PATTERN.matcher(template).replaceAll(matchResult ->
				resolvePlaceholder(matchResult.group(1), context)
						.orElse(matchResult.group(0)) // If resolution fails, keep the original placeholder
		);
	}

	private Optional<String> resolvePlaceholder(String placeholder, Map<String, Component> context) {
		String[] parts = placeholder.split("\\.");
		if (parts.length == 0) return Optional.empty();

		Component current = context.get(parts[0]);
		if (current == null) return Optional.empty();

		// Traverse the component structure for parts like {head.material.name}
		for (int i = 1; i < parts.length; i++) {
			String property = parts[i];

			// Check for final property "name"
			if ("name".equals(property)) {
				return Optional.of(getCleanName(current));
			}

			// Otherwise, traverse deeper
			if (current instanceof StructuredComponent structured) {
				// The part name (e.g., "material") is the key for the next component
				current = structured.structure().slots().get(OpenIdentifier.of("forgero", property))
						.map(slot -> slot.content())
						.orElse(null);
				if (current == null) return Optional.empty(); // Path traversal failed
			} else {
				return Optional.empty(); // Cannot traverse into a non-structured component
			}
		}

		// If the loop finishes, it means the placeholder was just one part (e.g., {material})
		// or the last part was a component itself. We resolve its name.
		return Optional.of(getCleanName(current));
	}

	private String getCleanName(Component component) {
		String name = component.id().name();
		// Special handling to trim suffixes for a cleaner name, important for palettes
		if (name.endsWith("_shape")) {
			return name.substring(0, name.length() - "_shape".length());
		}
		return name;
	}
}
