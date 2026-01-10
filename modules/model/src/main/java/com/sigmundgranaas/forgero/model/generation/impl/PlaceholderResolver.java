package com.sigmundgranaas.forgero.model.generation.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class PlaceholderResolver {
	private static final Pattern PATTERN = Pattern.compile("\\{([^}]+)}");

	public PlaceholderResolver() {
	}

	public String resolve(String template, Map<String, Component> context) {
		return resolve(template, context, Collections.emptyMap());
	}

	public String resolve(String template, Map<String, Component> context, Map<String, String> paletteMap) {
		if (template == null || template.isEmpty()) {
			return "";
		}
		Map<String, String> effectivePaletteMap = paletteMap != null ? paletteMap : Collections.emptyMap();
		return PATTERN.matcher(template).replaceAll(matchResult ->
				resolvePlaceholder(matchResult.group(1), context, effectivePaletteMap)
						.orElse(matchResult.group(0))
		);
	}

	private Optional<String> resolvePlaceholder(String placeholder, Map<String, Component> context, Map<String, String> paletteMap) {
		String[] parts = placeholder.split("\\.");
		if (parts.length == 0) return Optional.empty();

		Component current = context.get(parts[0]);
		if (current == null) return Optional.empty();

		for (int i = 1; i < parts.length; i++) {
			String property = parts[i];

			if ("name".equals(property)) {
				return Optional.of(getCleanName(current));
			}

			if ("palette".equals(property)) {
				return Optional.of(getPaletteName(current, paletteMap));
			}

			if (current instanceof StructuredComponent structured) {
				current = structured.structure().getPart(OpenIdentifier.of("forgero", property))
						.map(slot -> slot.content())
						.orElse(null);
				if (current == null) return Optional.empty();
			} else {
				return Optional.empty();
			}
		}

		return Optional.of(getCleanName(current));
	}

	private String getPaletteName(Component component, Map<String, String> paletteMap) {
		String name = getCleanName(component);
		return paletteMap.getOrDefault(name, name);
	}

	private String getCleanName(Component component) {
		String name = component.id().name();
		if (name.endsWith("_shape")) {
			return name.substring(0, name.length() - "_shape".length());
		}
		return name;
	}
}
