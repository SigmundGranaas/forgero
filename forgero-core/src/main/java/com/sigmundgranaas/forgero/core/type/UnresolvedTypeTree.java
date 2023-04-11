package com.sigmundgranaas.forgero.core.type;

public interface UnresolvedTypeTree extends MutableTypeTree {
	ResolvedTypeTree resolve();
}
