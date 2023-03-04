package com.sigmundgranaas.forgero.core.property.attribute;

import com.sigmundgranaas.forgero.core.property.Target;
import com.sigmundgranaas.forgero.core.property.TargetTypes;

import java.util.Set;

/**
 * Basic class for containing a single target, like a block.
 */
public record SingleTarget(TargetTypes target, Set<String> tags) implements Target {
	@Override
	public Set<TargetTypes> getTypes() {
		return Set.of(target);
	}

	@Override
	public Set<String> getTags() {
		return tags;
	}
}
