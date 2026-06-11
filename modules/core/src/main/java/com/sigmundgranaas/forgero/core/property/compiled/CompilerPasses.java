package com.sigmundgranaas.forgero.core.property.compiled;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.api.DataTypeEngine;
import com.sigmundgranaas.forgero.core.property.api.ResolutionKey;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * The registry of property compile passes the factory runs at component construction.
 *
 * <p>Each property type (on-hit, on-tick, loot, ...) contributes one pass — its
 * {@link DataTypeEngine}. The game modules register their passes here at load time, exactly
 * as they register property codecs. {@link ComponentCompiler} iterates the registered passes
 * when a terminal component is built, so the compiled artifact is complete before the item is
 * ever observed.
 *
 * <p>Attributes are compiled by a built-in pass and are not registered here.
 */
public final class CompilerPasses {
	private static final Map<OpenIdentifier, Supplier<? extends DataTypeEngine<?, ? extends java.util.List<?>>>> PASSES =
			new ConcurrentHashMap<>();

	private CompilerPasses() {
	}

	/**
	 * Registers a compile pass for a property type.
	 *
	 * @param key    The property type's resolution key.
	 * @param engine A supplier of the engine that compiles this property type.
	 */
	public static <P> void register(ResolutionKey<java.util.List<P>> key, Supplier<? extends DataTypeEngine<?, java.util.List<P>>> engine) {
		PASSES.put(key.id(), engine);
	}

	public static Map<OpenIdentifier, Supplier<? extends DataTypeEngine<?, ? extends java.util.List<?>>>> registered() {
		return PASSES;
	}
}
