package com.sigmundgranaas.forgero.core.state;

import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.state.composite.Construct;
import com.sigmundgranaas.forgero.core.state.upgrade.SimpleUpgrade;
import com.sigmundgranaas.forgero.core.type.Type;

import java.util.List;

public interface Upgrade extends State {
	static Upgrade of(Construct construct) {
		return new CompositeUpgrade(construct);
	}


	static Upgrade of(String name, Type type, List<Property> properties) {
		return new SimpleUpgrade(name, type, properties);
	}

}
