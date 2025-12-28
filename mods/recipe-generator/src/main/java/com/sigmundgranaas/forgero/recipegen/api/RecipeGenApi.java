package com.sigmundgranaas.forgero.recipegen.api;

import com.sigmundgranaas.forgero.recipegen.api.operation.OperationRegistry;
import com.sigmundgranaas.forgero.recipegen.api.template.TemplateLoader;
import com.sigmundgranaas.forgero.recipegen.api.variable.VariableConverterRegistry;
import com.sigmundgranaas.forgero.recipegen.impl.RecipeGenApiImpl;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * Main entry point for the Recipe Generator API.
 *
 * <p>This API provides a fluent, extensible interface for generating recipes
 * from JSON templates with variable substitution.</p>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * RecipeGenApi api = RecipeGenApi.getInstance();
 *
 * // Register custom variable converter
 * api.variables().register("mymod:custom_type", new MyCustomConverter());
 *
 * // Register custom operation
 * api.operations().register("mymod:item", "custom_op", myOperation);
 *
 * // Process templates
 * Collection<GeneratedRecipe> recipes = api.processor()
 *     .fromDirectory("recipe_generators")
 *     .withModLoadedCheck(FabricLoader.getInstance()::isModLoaded)
 *     .process();
 * }</pre>
 *
 * <h2>Extension Points</h2>
 * <ul>
 *   <li>{@link VariableConverterRegistry} - Register custom variable converters</li>
 *   <li>{@link OperationRegistry} - Register custom operations for variable transformation</li>
 *   <li>{@link TemplateLoader} - Provide custom template sources</li>
 * </ul>
 *
 * @see VariableConverterRegistry
 * @see OperationRegistry
 */
public interface RecipeGenApi {

	/**
	 * Gets the singleton instance of the RecipeGenApi.
	 *
	 * @return The RecipeGenApi instance
	 */
	static RecipeGenApi getInstance() {
		return RecipeGenApiImpl.getInstance();
	}

	/**
	 * Gets the variable converter registry for registering custom converters.
	 *
	 * @return The variable converter registry
	 */
	VariableConverterRegistry variables();

	/**
	 * Gets the operation registry for registering custom operations.
	 *
	 * @return The operation registry
	 */
	OperationRegistry operations();

	/**
	 * Creates a new template processor builder.
	 *
	 * @return A new TemplateProcessorBuilder
	 */
	TemplateProcessorBuilder processor();

	/**
	 * Builder for configuring template processing.
	 */
	interface TemplateProcessorBuilder {

		/**
		 * Sets the template source directory (relative to data/namespace/).
		 *
		 * @param directory The directory path (e.g., "recipe_generators")
		 * @return This builder
		 */
		TemplateProcessorBuilder fromDirectory(String directory);

		/**
		 * Sets the namespace for loading templates.
		 *
		 * @param namespace The namespace (e.g., "forgero")
		 * @return This builder
		 */
		TemplateProcessorBuilder namespace(String namespace);

		/**
		 * Sets a custom template loader.
		 *
		 * @param loader The template loader
		 * @return This builder
		 */
		TemplateProcessorBuilder withLoader(TemplateLoader loader);

		/**
		 * Sets the mod-loaded check predicate for dependency filtering.
		 *
		 * @param isModLoaded Predicate that checks if a mod ID is loaded
		 * @return This builder
		 */
		TemplateProcessorBuilder withModLoadedCheck(Predicate<String> isModLoaded);

		/**
		 * Enables or disables validation of generated recipes.
		 *
		 * @param validate Whether to validate (default: true in dev)
		 * @return This builder
		 */
		TemplateProcessorBuilder validateRecipes(boolean validate);

		/**
		 * Enables tracking of source templates for debugging.
		 *
		 * @param track Whether to track source templates
		 * @return This builder
		 */
		TemplateProcessorBuilder trackSourceTemplates(boolean track);

		/**
		 * Processes templates and returns generated recipes.
		 *
		 * @return Collection of generated recipes
		 */
		Collection<GeneratedRecipe> process();
	}
}
