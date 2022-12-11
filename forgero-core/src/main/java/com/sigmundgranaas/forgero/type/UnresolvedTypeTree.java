package com.sigmundgranaas.forgero.type;

public interface UnresolvedTypeTree extends MutableTypeTree {
    ResolvedTypeTree resolve();
}
