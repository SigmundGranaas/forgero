package com.sigmundgranaas.forgero.minecraft.common.recipe.implementation.generator;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringReplacer {
	private final Map<String, Function<Object, String>> replacementFunctions = new HashMap<>();

	public void register(String key, Function<Object, String> function) {
		replacementFunctions.put(key, function);
	}

	public String applyReplacements(String template, Map<String, Object> variableMap) {
		Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}");
		Matcher matcher = pattern.matcher(template);
		StringBuilder sb = new StringBuilder();

		while (matcher.find()) {
			String placeholder = matcher.group(1);
			String[] parts = placeholder.split("\\.");
			if (parts.length == 2 && variableMap.containsKey(parts[0]) && replacementFunctions.containsKey(parts[1])) {
				Object variable = variableMap.get(parts[0]);
				Function<Object, String> function = replacementFunctions.get(parts[1]);
				String replacement = function.apply(variable);
				matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
			} else if (parts.length == 1 && variableMap.containsKey(parts[0])) {
				Object variable = variableMap.get(parts[0]);
				String replacement = variable.toString();
				matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
			}
		}
		matcher.appendTail(sb);
		return sb.toString();
	}
}
