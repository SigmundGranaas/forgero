package com.sigmundgranaas.forgero.core.state;

public interface Identifiable {
	static Identifiable of(String name) {
		return new Identifiable() {
			@Override
			public String name() {
				return name;
			}

			@Override
			public String nameSpace() {
				return "";
			}
		};
	}

	String name();

	String nameSpace();

	default String identifier() {
		return String.format("%s:%s", nameSpace(), name());
	}
}
