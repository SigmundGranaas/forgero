package com.sigmundgranaas.forgero.model.generation.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderResolver {
	private static final Pattern PATTERN = Pattern.compile("\\{([^}]+)}");

	public String resolve(String template, Map<String, Object> context) {
		if (template == null || template.isEmpty()) {
			return "";
		}
		Matcher matcher = PATTERN.matcher(template);
		StringBuilder sb = new StringBuilder();
		while (matcher.find()) {
			String fullPath = matcher.group(1);
			Object resolvedValue = traversePath(context, fullPath);
			String replacement = resolvedValue != null ? String.valueOf(resolvedValue) : "";
			matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	private Object traversePath(Object root, String fullPath) {
		Object current = root;
		String[] pathParts = fullPath.split("\\.");
		for (String part : pathParts) {
			if (current == null) return null;

			if (current instanceof Map<?, ?> map) {
				current = map.get(part);
			} else {
				current = getProperty(current, part);
			}
		}
		return current;
	}

	private @Nullable Object getProperty(Object obj, String propertyName) {
		if (obj instanceof OpenIdentifier id) {
			return "name".equals(propertyName) ? id.name() : id.toString();
		} else if (obj instanceof Component comp) {
			return switch (propertyName) {
				case "id" -> comp.id();
				case "name" -> comp.id().name();
				default -> null;
			};
		}
		return null;
	}
}
