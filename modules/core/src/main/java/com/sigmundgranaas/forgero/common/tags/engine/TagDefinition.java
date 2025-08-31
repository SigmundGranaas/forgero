package com.sigmundgranaas.forgero.common.tags.engine;

import java.util.Collections;
import java.util.Set;

public record TagDefinition(Set<String> parents) {
	public TagDefinition {
		if (parents == null) {
			parents = Collections.emptySet();
		}
	}
}
