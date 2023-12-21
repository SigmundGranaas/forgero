package com.sigmundgranaas.forgero.generator.impl;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringReplacer {
	private final BiFunction<String, Object, String> variableReplacer;

	public StringReplacer(BiFunction<String, Object, String> variableReplacer) {
		this.variableReplacer = variableReplacer;
	}

	public String applyReplacements(String template, Map<String, Object> variableMap) {
		Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}");
		Matcher matcher = pattern.matcher(template);
		StringBuilder sb = new StringBuilder();

		while (matcher.find()) {
			String placeholder = matcher.group(1);
			String[] parts = placeholder.split("\\.");
			String variableKey = parts[0];
			if (parts.length == 2 && variableMap.containsKey(variableKey)) {
				Object variable = variableMap.get(variableKey);
				String operation = parts[1];
				String replacement = variableReplacer.apply(operation, variable);
				matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
			} else if (parts.length == 1 && variableMap.containsKey(variableKey)) {
				Object variable = variableMap.get(variableKey);
				String replacement = variable.toString();
				matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
			}
		}
		matcher.appendTail(sb);
		return sb.toString();
	}
}
