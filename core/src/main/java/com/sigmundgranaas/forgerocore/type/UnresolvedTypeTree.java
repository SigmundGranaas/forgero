package com.sigmundgranaas.forgerocore.type;

public interface UnresolvedTypeTree extends MutableTypeTree {
    ResolvedTypeTree resolve();
}
