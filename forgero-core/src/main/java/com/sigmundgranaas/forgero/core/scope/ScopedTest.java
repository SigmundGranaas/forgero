package com.sigmundgranaas.forgero.core.scope;

public interface ScopedTest {
	boolean isCompatible(Scope scope);
	boolean test(Scope scope);
}
