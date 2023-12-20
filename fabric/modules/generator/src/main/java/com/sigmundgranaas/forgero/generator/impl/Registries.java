package com.sigmundgranaas.forgero.generator.impl;

import java.util.Collection;
import java.util.function.Supplier;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.registry.GenericRegistry;
import com.sigmundgranaas.forgero.core.registry.LayeredConverterRegistry;
import com.sigmundgranaas.forgero.core.registry.MatchableConverterRegistry;

public class Registries {
	private static final MatchableConverterRegistry<JsonElement, Collection<?>> VARIABLE_CONVERTERS = new MatchableConverterRegistry<>();
	private static final LayeredConverterRegistry<Object, String> OPERATIONS = new LayeredConverterRegistry<>();
	private static final GenericRegistry<Supplier<Collection<IdentifiedJson>>> RECIPE_PROVIDERS = new GenericRegistry<>();


	public static MatchableConverterRegistry<JsonElement, Collection<?>> variableConverterRegistry() {
		return VARIABLE_CONVERTERS;
	}

	public static LayeredConverterRegistry<Object, String> operationRegistry() {
		return OPERATIONS;
	}

	public static GenericRegistry<Supplier<Collection<IdentifiedJson>>> recipeProviderRegistry() {
		return RECIPE_PROVIDERS;
	}
}
