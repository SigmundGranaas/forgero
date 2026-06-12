package com.sigmundgranaas.forgero.core.attribute.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeQueryResult;
import com.sigmundgranaas.forgero.core.attribute.api.BakedAttributes;
import com.sigmundgranaas.forgero.core.attribute.kernel.StatFold;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.EquipmentComponent;
import com.sigmundgranaas.forgero.core.property.api.CompilerPass;
import com.sigmundgranaas.forgero.core.property.api.ResolutionKey;

import java.util.List;
import java.util.stream.Stream;

/**
 * Read API and compiler pass for component attributes, backed by the {@link StatFold} kernel.
 *
 * <p>This is a thin adapter, not a computation engine: all attribute composition lives in
 * {@link StatFold} (the seal-fold stat kernel, see {@code docs/ADR-003-stat-contribution-kernel.md}).
 * Terminal components compile their attributes once at construction
 * (see {@code docs/ADR-002-compiler-in-the-factory.md}); the static helpers here read that
 * pre-compiled artifact in O(1) for {@link EquipmentComponent}s and fold on demand for the rare
 * non-terminal component (parts/materials shown in tooltips, derived comparison components).
 *
 * <p>Runtime state never enters this path. Game-state-dependent behaviour is modelled as effects
 * evaluated by the game layer.
 *
 * @see StatFold the stat kernel that performs all composition
 * @see BakedAttributes the compiled, O(1)-lookup result structure
 * @see CompilerPass the single-phase compile interface
 */
public class AttributeEngine implements CompilerPass<BakedAttributes> {
	public static final ResolutionKey<BakedAttributes> KEY = new ResolutionKey<>(new OpenIdentifier("forgero", "attributes"));

	public AttributeEngine() {
	}

	// ========== Static Convenience Methods ==========
	// O(1) lookup for EquipmentComponent (reads the compiled artifact), folding on demand for others.

	/**
	 * Gets the compiled value of a specific attribute from a component.
	 *
	 * @param component The component to query
	 * @param type      The attribute type
	 * @return The compiled attribute value, or 0 if not found
	 */
	public static float getAttribute(Component component, OpenIdentifier type) {
		if (component instanceof EquipmentComponent equipment) {
			return equipment.getAttribute(type);
		}
		return StatFold.fold(component).get(type).value();
	}

	/**
	 * Compiles all attributes for a component.
	 *
	 * @param component The component to compile
	 * @return Query result for accessing compiled attribute values
	 */
	public static AttributeQueryResult resolveAttributes(Component component) {
		BakedAttributes baked = component instanceof EquipmentComponent equipment
				? equipment.compiled().attributes()
				: StatFold.fold(component);
		return type -> baked.get(type).value();
	}

	@Override
	public ResolutionKey<BakedAttributes> key() {
		return KEY;
	}

	@Override
	public BakedAttributes compile(Stream<Component> components) {
		// The stream is the tree in root-first order; the kernel folds from the root.
		List<Component> componentList = components.toList();
		if (componentList.isEmpty()) {
			return BakedAttributes.EMPTY;
		}
		return StatFold.fold(componentList.get(0));
	}

}
