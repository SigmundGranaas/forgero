package com.sigmundgranaas.forgero.recipegen.impl.template;

import com.sigmundgranaas.forgero.recipegen.api.operation.OperationRegistry;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Replaces ${variable} and ${variable.operation} placeholders in strings.
 *
 * <p>Supports two placeholder formats:</p>
 * <ul>
 *   <li>{@code ${variable}} - Replaced with variable.toString()</li>
 *   <li>{@code ${variable.operation}} - Replaced using the registered operation</li>
 * </ul>
 */
public class StringReplacer {

	private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{(.*?)}");

	private final OperationRegistry operations;

	public StringReplacer(OperationRegistry operations) {
		this.operations = operations;
	}

	/**
	 * Applies variable replacements to the template string.
	 *
	 * @param template    The template string with placeholders
	 * @param variableMap Map of variable names to values
	 * @return The string with placeholders replaced
	 */
	public String applyReplacements(String template, Map<String, Object> variableMap) {
		Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
		StringBuilder result = new StringBuilder(template.length());

		int lastEnd = 0;
		while (matcher.find()) {
			result.append(template, lastEnd, matcher.start());

			String placeholder = matcher.group(1);
			String replacement = resolvePlaceholder(placeholder, variableMap);
			result.append(replacement);

			lastEnd = matcher.end();
		}

		if (lastEnd < template.length()) {
			result.append(template, lastEnd, template.length());
		}

		return result.toString();
	}

	private String resolvePlaceholder(String placeholder, Map<String, Object> variableMap) {
		int dotIndex = placeholder.indexOf('.');

		String variableKey = (dotIndex == -1) ? placeholder : placeholder.substring(0, dotIndex);
		Object variable = variableMap.get(variableKey);

		if (variable == null) {
			return "${" + placeholder + "}"; // Keep original if not found
		}

		if (dotIndex != -1) {
			String operation = placeholder.substring(dotIndex + 1);
			return operations.apply(operation, variable);
		} else {
			return variable.toString();
		}
	}
}
