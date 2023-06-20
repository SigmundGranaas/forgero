package com.sigmundgranaas.forgero.core.state;

import java.util.List;
import java.util.Optional;

import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.Target;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.SlotContainer;

public interface Composite extends Upgradeable<Composite>, State, CopyAble<Composite> {
	List<State> components();

	List<Property> compositeProperties(Target target);

	Optional<State> has(String id);

	SlotContainer getSlotContainer();
}
