package com.sigmundgranaas.forgero.generator.impl;

import java.util.Collection;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.registry.LayeredConverterRegistry;
import com.sigmundgranaas.forgero.core.registry.MatchableConverterRegistry;

public class Registries {
	private static final MatchableConverterRegistry<JsonElement, Collection<?>> VARIABLE_CONVERTERS = new MatchableConverterRegistry<>();
	private static final LayeredConverterRegistry<Object, String> OPERATIONS = new LayeredConverterRegistry<>();


	public static MatchableConverterRegistry<JsonElement, Collection<?>> variableConverterRegistry() {
		return VARIABLE_CONVERTERS;
	}

	public static LayeredConverterRegistry<Object, String> operationRegistry() {
		return OPERATIONS;
	}
}
