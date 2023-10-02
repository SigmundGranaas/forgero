package com.sigmundgranaas.forgero.core.state;

import java.util.List;
import java.util.Optional;

import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.SlotContainer;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

public interface Composite extends Upgradeable<Composite>, State, CopyAble<Composite> {
	List<State> components();

	List<Property> compositeProperties(Matchable target, MatchContext context);

	Optional<State> has(String id);

	SlotContainer getSlotContainer();
}
