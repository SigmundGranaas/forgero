package com.sigmundgranaas.forgero.core.property.attribute;

import com.sigmundgranaas.forgero.core.property.Target;
import com.sigmundgranaas.forgero.core.property.TargetTypes;

import java.util.Set;

/**
 * Target class for targeting a specific tool.
 * This class is primarily used for filtering attributes which belong to a specific tool when fetching all available attributes.
 */
public record ToolTarget(Set<String> targets) implements Target {
	@Override
	public Set<TargetTypes> getTypes() {
		return Set.of(TargetTypes.TOOL_TYPE);
	}

	@Override
	public Set<String> getTags() {
		return targets;
	}
}
