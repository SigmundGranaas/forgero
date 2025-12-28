package com.sigmundgranaas.forgero.core.component.api;

import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgrades;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgradeSlot;
import java.util.List;

/**
 * A contract for any component that can be customized with optional upgrades.
 */
public interface CustomizableComponent extends Component {
	/**
	 * @return The available upgrade slots on this component.
	 */
	ComponentUpgrades upgrades();

	/**
	 * Creates a new instance of this component with the specified upgrades.
	 *
	 * @param newUpgrades The new set of upgrades.
	 * @return A new component instance with the applied changes.
	 */
	Component withUpgrades(ComponentUpgrades newUpgrades);

	/**
	 * Convenience method to directly access the list of ComponentUpgradeSlot instances.
	 * Filters the underlying heterogeneous SlotContainer to only return upgrade slots.
	 *
	 * @return A list of ComponentUpgradeSlot instances.
	 */
	default List<ComponentUpgradeSlot> getUpgradeSlots() {
		return upgrades().slots().asList().stream()
				.filter(slot -> slot instanceof ComponentUpgradeSlot)
				.map(slot -> (ComponentUpgradeSlot) slot)
				.toList();
	}
}
