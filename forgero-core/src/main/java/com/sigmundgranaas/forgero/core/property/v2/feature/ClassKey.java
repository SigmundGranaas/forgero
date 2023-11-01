package com.sigmundgranaas.forgero.core.property.v2.feature;

public record ClassKey<T>(String type, Class<T> clazz) {

	@Override
	public int hashCode() {
		return type.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		ClassKey<?> classKey = (ClassKey<?>) obj;
		return type.equals(classKey.type);
	}
}
