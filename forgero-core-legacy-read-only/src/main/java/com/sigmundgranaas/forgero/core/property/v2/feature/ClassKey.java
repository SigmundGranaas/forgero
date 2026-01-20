package com.sigmundgranaas.forgero.core.property.v2.feature;

import com.sigmundgranaas.forgero.core.util.TypeToken;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class ClassKey<T> {
	private final String type;
	private final TypeToken<T> clazz;

	public ClassKey(String type, TypeToken<T> clazz) {
		this.type = type;
		this.clazz = clazz;
	}

	public ClassKey(String type, Class<T> clazz) {
		this.type = type;
		this.clazz = TypeToken.of(clazz);
	}

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
