package com.sigmundgranaas.forgero.core.property.v2;

import java.util.List;

import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

public interface PropertyProcessor {
	List<Property> process(List<Property> propertyList, Matchable target, MatchContext context);
}
