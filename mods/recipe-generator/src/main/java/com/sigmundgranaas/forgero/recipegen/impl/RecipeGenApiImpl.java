package com.sigmundgranaas.forgero.recipegen.impl;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.recipegen.api.GeneratedRecipe;
import com.sigmundgranaas.forgero.recipegen.api.RecipeGenApi;
import com.sigmundgranaas.forgero.recipegen.api.operation.OperationRegistry;
import com.sigmundgranaas.forgero.recipegen.api.template.TemplateLoader;
import com.sigmundgranaas.forgero.recipegen.api.variable.VariableConverterRegistry;
import com.sigmundgranaas.forgero.recipegen.impl.operation.OperationRegistryImpl;
import com.sigmundgranaas.forgero.recipegen.impl.template.TemplateProcessor;
import com.sigmundgranaas.forgero.recipegen.impl.variable.VariableConverterRegistryImpl;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;

/**
 * Singleton implementation of {@link RecipeGenApi}.
 */
public class RecipeGenApiImpl implements RecipeGenApi {

	private static final RecipeGenApiImpl INSTANCE = new RecipeGenApiImpl();

	private final VariableConverterRegistry variableRegistry;
	private final OperationRegistry operationRegistry;

	private RecipeGenApiImpl() {
		this.variableRegistry = new VariableConverterRegistryImpl();
		this.operationRegistry = new OperationRegistryImpl();
	}

	public static RecipeGenApiImpl getInstance() {
		return INSTANCE;
	}

	@Override
	public VariableConverterRegistry variables() {
		return variableRegistry;
	}

	@Override
	public OperationRegistry operations() {
		return operationRegistry;
	}

	@Override
	public TemplateProcessorBuilder processor() {
		return new TemplateProcessorBuilderImpl(variableRegistry, operationRegistry);
	}

	private static class TemplateProcessorBuilderImpl implements TemplateProcessorBuilder {
		private final VariableConverterRegistry variables;
		private final OperationRegistry operations;

		private String directory = "recipe_generators";
		private String namespace = "forgero";
		private TemplateLoader loader;
		private Predicate<String> isModLoaded = s -> true;
		private boolean validate = true;
		private boolean trackSourceTemplates = false;

		TemplateProcessorBuilderImpl(VariableConverterRegistry variables,
		                             OperationRegistry operations) {
			this.variables = variables;
			this.operations = operations;
		}

		@Override
		public TemplateProcessorBuilder fromDirectory(String directory) {
			this.directory = directory;
			return this;
		}

		@Override
		public TemplateProcessorBuilder namespace(String namespace) {
			this.namespace = namespace;
			return this;
		}

		@Override
		public TemplateProcessorBuilder withLoader(TemplateLoader loader) {
			this.loader = loader;
			return this;
		}

		@Override
		public TemplateProcessorBuilder withModLoadedCheck(Predicate<String> isModLoaded) {
			this.isModLoaded = isModLoaded;
			return this;
		}

		@Override
		public TemplateProcessorBuilder validateRecipes(boolean validate) {
			this.validate = validate;
			return this;
		}

		@Override
		public TemplateProcessorBuilder trackSourceTemplates(boolean track) {
			this.trackSourceTemplates = track;
			return this;
		}

		@Override
		public Collection<GeneratedRecipe> process() {
			TemplateProcessor processor = new TemplateProcessor(
					variables, operations, isModLoaded, trackSourceTemplates
			);

			Collection<JsonObject> templates;
			if (loader != null) {
				templates = loader.load(namespace, directory);
			} else {
				// No loader provided - return empty
				// In real usage, a loader would be injected or resource manager used
				templates = Collections.emptyList();
			}

			return processor.processTemplates(templates);
		}
	}
}
