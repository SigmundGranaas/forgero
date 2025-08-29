package com.sigmundgranaas.forgero.common.env;

import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.core.component.mutation.api.ComponentMutater;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;

/**
 * A static service locator to provide access to essential Forgero services
 * from contexts where dependency injection is not possible, such as recipe crafting.
 *
 * This class must be initialized by the loader module once all services are ready.
 */
public class ForgeroEnvironment {
	private static ComponentRegistry componentRegistry;
	private static ComponentConverter componentConverter;
	private static ComponentMutater componentMutater;
	private static boolean initialized = false;

	public static void initialize(ComponentRegistry registry, ComponentConverter converter, ComponentMutater mutater) {
		if (initialized) {
			throw new IllegalStateException("ForgeroEnvironment is already initialized.");
		}
		componentRegistry = registry;
		componentConverter = converter;
		componentMutater = mutater;
		initialized = true;
	}

	private static void checkInitialized() {
		if (!initialized) {
			throw new IllegalStateException("ForgeroEnvironment has not been initialized. This is a critical error.");
		}
	}

	public static ComponentRegistry getComponentRegistry() {
		checkInitialized();
		return componentRegistry;
	}

	public static ComponentConverter getComponentConverter() {
		checkInitialized();
		return componentConverter;
	}

	public static ComponentMutater getComponentMutater() {
		checkInitialized();
		return componentMutater;
	}
}
