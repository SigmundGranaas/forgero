package com.sigmundgranaas.forgero.generator;

import static com.sigmundgranaas.forgero.generator.api.Registry.recipeSupplier;
import static com.sigmundgranaas.forgero.generator.api.Registry.variableConverter;

import com.sigmundgranaas.forgero.generator.impl.DataDirectoryRecipeGenerator;
import com.sigmundgranaas.forgero.generator.impl.Registries;
import com.sigmundgranaas.forgero.generator.impl.StringReplacer;
import com.sigmundgranaas.forgero.generator.impl.VariableToMapTransformer;
import com.sigmundgranaas.forgero.generator.impl.converter.StringListVariableConverter;

import net.fabricmc.api.ModInitializer;

public class GeneratorInitializer implements ModInitializer {

	@Override
	public void onInitialize() {
		variableConverter("forgero:string_list", StringListVariableConverter::new);

		StringReplacer stringReplacer = new StringReplacer(Registries.operationRegistry()::convert);
		VariableToMapTransformer transformer = new VariableToMapTransformer(Registries.variableConverterRegistry()::convert);
		DataDirectoryRecipeGenerator recipeGenerator = new DataDirectoryRecipeGenerator(stringReplacer, transformer, "/data/forgero/recipe_generators");

		recipeSupplier("forgero:datapack_recipe_generator", recipeGenerator::generate);
	}
}
