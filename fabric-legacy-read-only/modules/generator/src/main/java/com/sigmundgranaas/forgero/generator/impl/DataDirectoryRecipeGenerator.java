package com.sigmundgranaas.forgero.generator.impl;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.generator.impl.recipe.validation.RecipeValidator;

import java.util.Collection;
import java.util.stream.Collectors;

public class DataDirectoryRecipeGenerator {
		private final String directory;
		private final ResourceManagerJsonLoader loader;
		private final RecipeValidator recipeValidator;
		private final RecipeGenerator recipeGenerator;

		public DataDirectoryRecipeGenerator(String directory, ResourceManagerJsonLoader loader, RecipeGenerator recipeGenerator) {
			this.directory = directory;
			this.loader = loader;
			this.recipeGenerator = recipeGenerator;
			this.recipeValidator = new RecipeValidator();
		}


	public Collection<IdentifiedJson> generate() {
		long conversionTime = System.nanoTime();
		var loadedTemplates = loader.load(directory);
		var result = loadedTemplates
						.parallelStream()
						.flatMap(template -> recipeGenerator.generateRecipeFrom(template).stream())
						.filter(recipeValidator::validateRecipe)
				.collect(Collectors.toList());

		long convertEnd = System.nanoTime();

		long processingTime = (convertEnd - conversionTime) / 1_000_000;
		Forgero.LOGGER.info("Converted {} recipes from {} templates recipes in {}ms", result.size(), loadedTemplates.size(), processingTime);
		return result;
	}
}
