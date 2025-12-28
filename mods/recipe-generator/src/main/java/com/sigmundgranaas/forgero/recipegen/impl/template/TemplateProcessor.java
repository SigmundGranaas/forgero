package com.sigmundgranaas.forgero.recipegen.impl.template;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.recipegen.api.GeneratedRecipe;
import com.sigmundgranaas.forgero.recipegen.api.operation.OperationRegistry;
import com.sigmundgranaas.forgero.recipegen.api.variable.VariableConverterRegistry;
import com.sigmundgranaas.forgero.recipegen.impl.CartesianProductCombinator;

import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Processes recipe templates and generates recipes from variable combinations.
 */
public class TemplateProcessor {

	private static final String VARIABLES_KEY = "variables";
	private static final String IDENTIFIER_KEY = "identifier";
	private static final String DEPENDS_ON_KEY = "depends_on";

	private static final Gson GSON = new Gson();

	private final VariableConverterRegistry variableRegistry;
	private final OperationRegistry operationRegistry;
	private final CartesianProductCombinator combinator;
	private final StringReplacer replacer;
	private final Predicate<String> isModLoaded;
	private final boolean trackSourceTemplates;

	public TemplateProcessor(
			VariableConverterRegistry variableRegistry,
			OperationRegistry operationRegistry,
			Predicate<String> isModLoaded,
			boolean trackSourceTemplates) {
		this.variableRegistry = variableRegistry;
		this.operationRegistry = operationRegistry;
		this.combinator = new CartesianProductCombinator(variableRegistry);
		this.replacer = new StringReplacer(operationRegistry);
		this.isModLoaded = isModLoaded;
		this.trackSourceTemplates = trackSourceTemplates;
	}

	/**
	 * Processes a collection of templates and generates recipes.
	 *
	 * @param templates The templates to process
	 * @return Collection of generated recipes
	 */
	public Collection<GeneratedRecipe> processTemplates(Collection<JsonObject> templates) {
		List<GeneratedRecipe> results = new ArrayList<>();

		for (JsonObject template : templates) {
			if (shouldSkipTemplate(template)) {
				continue;
			}

			results.addAll(processTemplate(template));
		}

		return results;
	}

	/**
	 * Processes a single template and generates recipes.
	 *
	 * @param template The template to process
	 * @return Collection of generated recipes
	 */
	public Collection<GeneratedRecipe> processTemplate(JsonObject template) {
		List<GeneratedRecipe> results = new ArrayList<>();

		// Check dependencies first
		if (shouldSkipTemplate(template)) {
			return results;
		}

		if (!template.has(VARIABLES_KEY)) {
			// No variables - just create single recipe
			GeneratedRecipe recipe = createRecipe(template, Map.of(), template);
			if (recipe != null) {
				results.add(recipe);
			}
			return results;
		}

		JsonObject variables = template.getAsJsonObject(VARIABLES_KEY);
		Collection<Map<String, Object>> combinations = combinator.generateCombinations(variables);

		for (Map<String, Object> combination : combinations) {
			GeneratedRecipe recipe = createRecipe(template, combination, template);
			if (recipe != null) {
				results.add(recipe);
			}
		}

		return results;
	}

	private GeneratedRecipe createRecipe(JsonObject template, Map<String, Object> variables, JsonObject sourceTemplate) {
		// Convert template to string for replacement
		String templateString = GSON.toJson(template);

		// Apply variable replacements
		String processedString = replacer.applyReplacements(templateString, variables);

		// Parse back to JSON
		JsonObject processedJson = GSON.fromJson(processedString, JsonObject.class);

		// Remove metadata fields that shouldn't be in final recipe
		processedJson.remove(VARIABLES_KEY);
		processedJson.remove(DEPENDS_ON_KEY);

		// Extract identifier
		if (!processedJson.has(IDENTIFIER_KEY)) {
			return null;
		}

		String identifierString = processedJson.get(IDENTIFIER_KEY).getAsString();
		processedJson.remove(IDENTIFIER_KEY);

		Identifier id = new Identifier(identifierString);

		return GeneratedRecipe.of(
				id,
				processedJson,
				trackSourceTemplates ? sourceTemplate : null
		);
	}

	private boolean shouldSkipTemplate(JsonObject template) {
		if (template.has(DEPENDS_ON_KEY)) {
			JsonElement dependsOn = template.get(DEPENDS_ON_KEY);
			if (dependsOn.isJsonArray()) {
				for (JsonElement dep : dependsOn.getAsJsonArray()) {
					if (!isModLoaded.test(dep.getAsString())) {
						return true;
					}
				}
			} else if (dependsOn.isJsonPrimitive()) {
				if (!isModLoaded.test(dependsOn.getAsString())) {
					return true;
				}
			}
		}
		return false;
	}
}
