package com.sigmundgranaas.forgero.core.component.api;

import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;
import java.util.List;

/**
 * An interface for any ForgeroComponent that is composed of other, required components.
 * This provides a standard way to access the "children" of a component.
 */
public interface StructuredComponent extends Component {
	/**
	 * @return The defined structure of this component.
	 */
	ComponentStructure structure();

	/**
	 * Creates a new instance of this component with the specified structure.
	 *
	 * @param newStructure The new structure.
	 * @return A new component instance with the applied changes.
	 */
	Component withStructure(ComponentStructure newStructure);

	/**
	 * Overrides the default getChildren to return components from its structure.
	 */
	@Override
	default List<Component> getChildren() {
		return structure().children();
	}
}
