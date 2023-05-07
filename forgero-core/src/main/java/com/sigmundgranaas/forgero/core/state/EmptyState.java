package com.sigmundgranaas.forgero.core.state;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.customdata.DataContainer;
import com.sigmundgranaas.forgero.core.property.Target;
import com.sigmundgranaas.forgero.core.type.Type;

public class EmptyState implements State {

	public static State EMPTY_STATE = new EmptyState();

	@Override
	public String name() {
		return "empty";
	}

	@Override
	public String nameSpace() {
		return Forgero.NAMESPACE;
	}

	@Override
	public Type type() {
		return Type.UNDEFINED;
	}

	@Override
	public DataContainer customData(Target target) {
		return DataContainer.empty();
	}
}
