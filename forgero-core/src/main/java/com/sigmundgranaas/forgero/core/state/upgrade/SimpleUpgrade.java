package com.sigmundgranaas.forgero.core.state.upgrade;

import java.util.List;

import com.sigmundgranaas.forgero.core.customdata.DataContainer;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.Target;
import com.sigmundgranaas.forgero.core.state.Upgrade;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import org.jetbrains.annotations.NotNull;

public class SimpleUpgrade implements Upgrade {
	private final String name;
	private final Type type;
	private final List<Property> properties;

	public SimpleUpgrade(String name, Type type, List<Property> properties) {
		this.name = name;
		this.type = type;
		this.properties = properties;
	}

	@Override
	public @NotNull
	List<Property> getRootProperties() {
		return properties;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public String nameSpace() {
		return "forgero";
	}


	@Override
	public boolean test(Matchable match, MatchContext context) {
		return type.test(match, context);
	}

	@Override
	public Type type() {
		return type;
	}

	@Override
	public DataContainer customData(Target target) {
		return DataContainer.empty();
	}
}
