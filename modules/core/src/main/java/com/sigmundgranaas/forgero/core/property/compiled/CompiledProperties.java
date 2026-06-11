package com.sigmundgranaas.forgero.core.property.compiled;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.BakedAttributes;
import com.sigmundgranaas.forgero.core.property.api.ResolutionKey;

import java.util.List;
import java.util.Map;

/**
 * The compiled output of a component tree: the artifact the game layer reads.
 *
 * <p>Produced once by {@link ComponentCompiler} when a terminal component is constructed
 * (craft, part swap, upgrade socket, NBT deserialization). It holds the fully compiled
 * attributes plus the compiled property lists for every registered property type. All
 * static conditions have been resolved away during compilation; any dynamic conditions are
 * carried on the property objects as data, for the game layer to evaluate.
 *
 * <p>This is the outermost layer described in {@code docs/ADR-002-compiler-in-the-factory.md}:
 * runtime queries read this map and never traverse or re-resolve the component tree.
 *
 * @param attributes      Compiled attributes (O(1) lookup by type).
 * @param propertiesByKey Compiled property lists keyed by resolution-key id.
 */
public record CompiledProperties(
		BakedAttributes attributes,
		Map<OpenIdentifier, List<?>> propertiesByKey
) {
	public static final CompiledProperties EMPTY = new CompiledProperties(BakedAttributes.EMPTY, Map.of());

	public CompiledProperties {
		propertiesByKey = Map.copyOf(propertiesByKey);
	}

	/**
	 * Reads the compiled property list for a resolution key.
	 *
	 * @return The compiled properties (including any carrying dynamic conditions as data),
	 * or an empty list if this item declares none of that type.
	 */
	@SuppressWarnings("unchecked")
	public <P> List<P> get(ResolutionKey<List<P>> key) {
		return (List<P>) propertiesByKey.getOrDefault(key.id(), List.of());
	}
}
