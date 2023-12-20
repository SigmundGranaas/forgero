package com.sigmundgranaas.forgero.generator.api;

import static com.sigmundgranaas.forgero.generator.impl.Registries.*;

import java.util.Collection;
import java.util.function.Supplier;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.registry.GenericRegistry;
import com.sigmundgranaas.forgero.core.registry.RankableConverter;
import com.sigmundgranaas.forgero.generator.impl.IdentifiedJson;

public interface Registry {
	static GenericRegistry.RegisteredReference<RankableConverter<JsonElement, Collection<?>>> variableConverter(String name, RankableConverter<JsonElement, Collection<?>> converter) {
		return variableConverterRegistry().register(name, converter);
	}

	static GenericRegistry.RegisteredReference<RankableConverter<JsonElement, Collection<?>>> variableConverter(String name, Supplier<RankableConverter<JsonElement, Collection<?>>> converter) {
		return variableConverter(name, converter.get());
	}

	static GenericRegistry.RegisteredReference<RankableConverter<Object, String>> operation(String id, String operation, RankableConverter<Object, String> implementation) {
		return operationRegistry().register(id, operation, implementation);
	}

	static GenericRegistry.RegisteredReference<Supplier<Collection<IdentifiedJson>>> recipeSupplier(String id, Supplier<Collection<IdentifiedJson>> generator) {
		return recipeProviderRegistry().register(id, generator);
	}
}
