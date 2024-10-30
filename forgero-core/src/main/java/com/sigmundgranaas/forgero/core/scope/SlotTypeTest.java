package com.sigmundgranaas.forgero.core.scope;


public class SlotTypeTest implements ScopedTest {
	private String type;


	@Override
	public boolean isCompatible(Scope scope) {
		return scope.identifier().equals("forgero:slot");
	}

	@Override
	public boolean test(Scope scope) {
		return scope.identifier().equals("forgero:slot");
	}
}
