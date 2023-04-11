package com.sigmundgranaas.forgero.core.property.v2.cache;

import static com.sigmundgranaas.forgero.core.util.Identifiers.EMPTY_IDENTIFIER;

public interface CacheAbleKey {
	CacheAbleKey EMPTY = () -> EMPTY_IDENTIFIER;

	String key();
}
