package com.sigmundgranaas.forgero.generator.impl;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringReplacer {
	private final BiFunction<String, Object, String> variableReplacer;
	private final Pattern pattern;

	public StringReplacer(BiFunction<String, Object, String> variableReplacer) {
		this.variableReplacer = variableReplacer;
		this.pattern = Pattern.compile("\\$\\{(.*?)}");
	}

	public String applyReplacements(String template, Map<String, Object> variableMap) {
		Matcher matcher = pattern.matcher(template);
		StringBuilder sb = new StringBuilder(template.length());

		int lastEnd = 0;
		while (matcher.find()) {
			sb.append(template, lastEnd, matcher.start());
			String placeholder = matcher.group(1);
			int dotIndex = placeholder.indexOf('.');

			String variableKey = (dotIndex == -1) ? placeholder : placeholder.substring(0, dotIndex);
			Object variable = variableMap.get(variableKey);

			if (variable != null) {
				String replacement;
				if (dotIndex != -1) {
					String operation = placeholder.substring(dotIndex + 1);
					replacement = variableReplacer.apply(operation, variable);
				} else {
					replacement = variable.toString();
				}
				sb.append(replacement);
			} else {
				sb.append(matcher.group());  // Keep original if no replacement found
			}

			lastEnd = matcher.end();
		}

		if (lastEnd < template.length()) {
			sb.append(template, lastEnd, template.length());
		}

		return sb.toString();
	}
}


