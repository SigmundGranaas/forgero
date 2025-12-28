package com.sigmundgranaas.forgero.recipegen.api.template;

import com.google.gson.JsonObject;

import java.util.Collection;

/**
 * Loads recipe templates from a source.
 *
 * <p>Implementations may load templates from resource managers, file systems,
 * or other sources.</p>
 *
 * <h2>Template Format</h2>
 * Templates are JSON objects that may contain:
 * <ul>
 *   <li>{@code variables} - Map of variable definitions</li>
 *   <li>{@code type} - Recipe type identifier</li>
 *   <li>{@code identifier} - Recipe identifier pattern with placeholders</li>
 *   <li>Recipe-specific fields with {@code ${variable}} placeholders</li>
 * </ul>
 *
 * <h2>Example Template</h2>
 * <pre>{@code
 * {
 *   "variables": {
 *     "material": {"type": "TOOL_MATERIAL"},
 *     "variant": ["refined", "mastercrafted"]
 *   },
 *   "identifier": "forgero:${material.name}-${variant}-sword_blade",
 *   "type": "minecraft:crafting_shaped",
 *   "pattern": ["MMM", " S ", " S "],
 *   "key": {
 *     "M": {"item": "${material.item}"},
 *     "S": {"item": "minecraft:stick"}
 *   },
 *   "result": {"item": "forgero:${material.name}-${variant}_sword_blade"}
 * }
 * }</pre>
 */
public interface TemplateLoader {

	/**
	 * Loads all templates from the specified path.
	 *
	 * @param path The path to load templates from (e.g., "recipe_generators")
	 * @return Collection of template JSON objects
	 */
	Collection<JsonObject> load(String path);

	/**
	 * Loads templates from a specific namespace and path.
	 *
	 * @param namespace The namespace (e.g., "forgero")
	 * @param path      The path within the namespace
	 * @return Collection of template JSON objects
	 */
	Collection<JsonObject> load(String namespace, String path);
}
