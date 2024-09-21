package com.sigmundgranaas.forgero.generator.api;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.registry.GenericRegistry;
import com.sigmundgranaas.forgero.core.registry.RankableConverter;

import java.util.Collection;
import java.util.function.Supplier;

import static com.sigmundgranaas.forgero.generator.impl.Registries.operationRegistry;
import static com.sigmundgranaas.forgero.generator.impl.Registries.variableConverterRegistry;

/**
 * Simple registry interface for registering variable converters and operations for these variables.
 * <p>
 * Example:
 * <pre>
 * {@code
 *  variableConverter("forgero:string_list", StringListVariableConverter::new);
 *  operation("minecraft:identifier", "identifier", itemOperationFactory.build(identifier));
 * }
 * </pre>
 */
public interface GeneratorRegistry {
	static GenericRegistry.RegisteredReference<RankableConverter<JsonElement, Collection<?>>> variableConverter(String name, RankableConverter<JsonElement, Collection<?>> converter) {
		return variableConverterRegistry().register(name, converter);
	}

	static GenericRegistry.RegisteredReference<RankableConverter<JsonElement, Collection<?>>> variableConverter(String name, Supplier<RankableConverter<JsonElement, Collection<?>>> converter) {
		return variableConverter(name, converter.get());
	}

	static GenericRegistry.RegisteredReference<RankableConverter<Object, String>> operation(String id, String operation, RankableConverter<Object, String> implementation) {
		return operationRegistry().register(id, operation, implementation);
	}
}
