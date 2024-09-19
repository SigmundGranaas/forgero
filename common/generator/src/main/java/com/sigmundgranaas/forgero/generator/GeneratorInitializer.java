package com.sigmundgranaas.forgero.generator;

import java.util.function.Function;

import com.sigmundgranaas.forgero.core.api.v0.entrypoint.ForgeroPreInitializedEntryPoint;
import com.sigmundgranaas.forgero.generator.api.operation.OperationFactory;
import com.sigmundgranaas.forgero.generator.impl.converter.StringListVariableConverter;
import com.sigmundgranaas.forgero.generator.impl.converter.TagToItemConverter;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;

import static com.sigmundgranaas.forgero.generator.api.GeneratorRegistry.operation;
import static com.sigmundgranaas.forgero.generator.api.GeneratorRegistry.variableConverter;

public class GeneratorInitializer implements ForgeroPreInitializedEntryPoint {
	@Override
	public void onPreInitialized() {
		minecraftSetup();
	}

	private void minecraftSetup() {
		variableConverter("forgero:string_list", StringListVariableConverter::new);
		variableConverter("minecraft:tags", TagToItemConverter::new);

		OperationFactory<Item> itemOperationFactory = new OperationFactory<>(Item.class);
		Function<Item, String> identifier = item -> Registries.ITEM.getId(item).toString();
		operation("minecraft:identifier", "identifier", itemOperationFactory.build(identifier));
		operation("minecraft:identifier", "id", itemOperationFactory.build(identifier));
	}
}
