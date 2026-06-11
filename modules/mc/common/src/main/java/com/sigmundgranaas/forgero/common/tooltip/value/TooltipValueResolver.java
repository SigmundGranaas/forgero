package com.sigmundgranaas.forgero.common.tooltip.value;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tooltip.display.TooltipTextFormatter;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeQueryResult;
import com.sigmundgranaas.forgero.core.component.api.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves dynamic placeholders in tooltip templates to actual values.
 * <p>
 * Supports patterns like:
 * <ul>
 *   <li>{@code {damage}} - resolve from attributes</li>
 *   <li>{@code {speed:%.2f}} - with Java format specifier</li>
 *   <li>{@code {material_name}} - from custom placeholder registry</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * TooltipValueResolver resolver = new TooltipValueResolver(component, attributes);
 *
 * // Resolve placeholders in a template
 * String resolved = resolver.resolve("Deals {attack_damage} damage");
 * // Result: "Deals 7 damage"
 *
 * // Get arguments for Text.translatable()
 * Object[] args = resolver.resolveArgs(List.of("attack_damage", "attack_speed"));
 * Text text = Text.translatable("my.tooltip.key", args);
 * }</pre>
 */
public class TooltipValueResolver {

	private static final Logger LOGGER = LoggerFactory.getLogger(TooltipValueResolver.class);

	/**
	 * Pattern for placeholders: {name} or {name:format}
	 * Group 1: placeholder name
	 * Group 2: optional format specifier (without the colon)
	 */
	private static final Pattern PLACEHOLDER_PATTERN =
			Pattern.compile("\\{([a-zA-Z_][a-zA-Z0-9_]*)(?::([^}]+))?\\}");

	private final Component component;
	private final AttributeQueryResult attributes;

	/**
	 * Creates a resolver for the given component.
	 *
	 * @param component  The component being rendered
	 * @param attributes The resolved attributes for the component
	 */
	public TooltipValueResolver(Component component, AttributeQueryResult attributes) {
		this.component = component;
		this.attributes = attributes;
	}

	/**
	 * Resolves all placeholders in a template string.
	 * <p>
	 * Placeholders that cannot be resolved are left unchanged.
	 *
	 * @param template The template with placeholders like "Deals {damage} damage"
	 * @return The resolved string with actual values
	 */
	public String resolve(String template) {
		if (template == null || template.isEmpty()) {
			return template;
		}

		Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
		StringBuilder result = new StringBuilder();

		while (matcher.find()) {
			String placeholderName = matcher.group(1);
			String format = matcher.group(2); // may be null

			String replacement = resolveValue(placeholderName, format)
					.orElse(matcher.group(0)); // Keep original if not resolved

			matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
		}
		matcher.appendTail(result);

		return result.toString();
	}

	/**
	 * Resolves placeholder names to values for use with {@code Text.translatable()}.
	 * <p>
	 * The names should correspond to the %s placeholders in the translation value.
	 *
	 * @param placeholderNames List of placeholder names to resolve
	 * @return Array of resolved values for use as translation arguments
	 */
	public Object[] resolveArgs(List<String> placeholderNames) {
		return placeholderNames.stream()
				.map(name -> resolveValue(name, null).orElse("?"))
				.toArray();
	}

	/**
	 * Resolves a single placeholder name to its formatted value.
	 *
	 * @param name   The placeholder name (e.g., "attack_damage")
	 * @param format Optional format specifier (e.g., "%.2f")
	 * @return The resolved value, or empty if not found
	 */
	public Optional<String> resolveValue(String name, String format) {
		// 1. Try attribute lookup first
		OpenIdentifier attrId = OpenIdentifier.parse("forgero:" + name.toLowerCase());
		float attrValue = attributes.getValue(attrId);

		// Check if this attribute exists (non-zero or known)
		if (attrValue != 0f) {
			return Optional.of(formatValue(attrValue, format));
		}

		// 2. Try without namespace prefix
		attrId = OpenIdentifier.parse(name.toLowerCase());
		attrValue = attributes.getValue(attrId);
		if (attrValue != 0f) {
			return Optional.of(formatValue(attrValue, format));
		}

		// 3. Try custom placeholder registry
		Optional<Object> customValue = PlaceholderRegistry.resolve(name, component);
		if (customValue.isPresent()) {
			return Optional.of(formatValue(customValue.get(), format));
		}

		return Optional.empty();
	}

	/**
	 * Gets the raw value for a placeholder without formatting.
	 *
	 * @param name The placeholder name
	 * @return The raw value, or empty if not found
	 */
	public Optional<Object> getRawValue(String name) {
		// Try attribute
		OpenIdentifier attrId = OpenIdentifier.parse("forgero:" + name.toLowerCase());
		float attrValue = attributes.getValue(attrId);
		if (attrValue != 0f) {
			return Optional.of(attrValue);
		}

		// Try without prefix
		attrId = OpenIdentifier.parse(name.toLowerCase());
		attrValue = attributes.getValue(attrId);
		if (attrValue != 0f) {
			return Optional.of(attrValue);
		}

		// Try custom
		return PlaceholderRegistry.resolve(name, component);
	}

	/**
	 * Extracts all placeholder names from a template.
	 *
	 * @param template The template to scan
	 * @return List of placeholder names found
	 */
	public static List<String> extractPlaceholders(String template) {
		List<String> placeholders = new ArrayList<>();
		if (template == null || template.isEmpty()) {
			return placeholders;
		}

		Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
		while (matcher.find()) {
			placeholders.add(matcher.group(1));
		}
		return placeholders;
	}

	/**
	 * Formats a value with an optional format specifier.
	 */
	private String formatValue(Object value, String format) {
		if (format == null || format.isEmpty()) {
			// Default formatting
			if (value instanceof Float f) {
				return TooltipTextFormatter.formatFloat(f);
			}
			if (value instanceof Double d) {
				return TooltipTextFormatter.formatFloat(d.floatValue());
			}
			if (value instanceof Number n) {
				return TooltipTextFormatter.formatFloat(n.floatValue());
			}
			return String.valueOf(value);
		}

		// Use provided format specifier
		try {
			return String.format(format, value);
		} catch (Exception e) {
			LOGGER.debug("Invalid format specifier '{}' for value '{}': {}",
					format, value, e.getMessage());
			return String.valueOf(value);
		}
	}
}
