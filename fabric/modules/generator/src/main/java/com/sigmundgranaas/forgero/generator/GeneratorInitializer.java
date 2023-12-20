package com.sigmundgranaas.forgero.generator;

import static com.sigmundgranaas.forgero.generator.api.Registry.*;

import java.util.function.Function;

import com.sigmundgranaas.forgero.generator.api.operation.OperationFactory;
import com.sigmundgranaas.forgero.generator.impl.DataDirectoryRecipeGenerator;
import com.sigmundgranaas.forgero.generator.impl.Registries;
import com.sigmundgranaas.forgero.generator.impl.StringReplacer;
import com.sigmundgranaas.forgero.generator.impl.VariableToMapTransformer;
import com.sigmundgranaas.forgero.generator.impl.converter.StringListVariableConverter;
import com.sigmundgranaas.forgero.generator.impl.converter.TagToItemConverter;

import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

import net.fabricmc.api.ModInitializer;

public class GeneratorInitializer implements ModInitializer {

	@Override
	public void onInitialize() {
		minecraftSetup();
		StringReplacer stringReplacer = new StringReplacer(Registries.operationRegistry()::convert);
		VariableToMapTransformer transformer = new VariableToMapTransformer(Registries.variableConverterRegistry()::convert);
		DataDirectoryRecipeGenerator recipeGenerator = new DataDirectoryRecipeGenerator(stringReplacer, transformer, "/data/forgero/recipe_generators");

		recipeSupplier("forgero:datapack_recipe_generator", recipeGenerator::generate);
	}


	private void minecraftSetup() {
		variableConverter("forgero:string_list", StringListVariableConverter::new);
		variableConverter("minecraft:tags", TagToItemConverter::new);

		OperationFactory<Item> itemOperationFactory = new OperationFactory<>(Item.class);
		Function<Item, String> identifier = item -> Registry.ITEM.getId(item).toString();
		operation("minecraft:identifier", "identifier", itemOperationFactory.build(identifier));
		operation("minecraft:identifier", "id", itemOperationFactory.build(identifier));

	}
}
