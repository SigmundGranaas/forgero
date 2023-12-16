package com.sigmundgranaas.forgero.minecraft.common.recipe.implementation.generator;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sigmundgranaas.forgero.core.state.State;

public class StringReplacer {
	private final Map<String, Function<State, String>> replacementFunctions = new HashMap<>();

	public void register(String key, Function<State, String> function) {
		replacementFunctions.put(key, function);
	}

	public String applyReplacements(String template, Map<String, State> stateMap) {
		Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}");
		Matcher matcher = pattern.matcher(template);
		StringBuilder sb = new StringBuilder();

		while (matcher.find()) {
			String[] parts = matcher.group(1).split("\\.");
			if (parts.length == 2 && stateMap.containsKey(parts[0]) && replacementFunctions.containsKey(parts[1])) {
				State state = stateMap.get(parts[0]);
				Function<State, String> function = replacementFunctions.get(parts[1]);
				String replacement = function.apply(state);
				matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
			}
		}
		matcher.appendTail(sb);
		return sb.toString();
	}
}
