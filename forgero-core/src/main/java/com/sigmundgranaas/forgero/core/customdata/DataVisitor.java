package com.sigmundgranaas.forgero.core.customdata;

import java.util.Optional;

public interface DataVisitor<T> {
	Optional<T> visit(DataContainer dataContainer);

	String key();

	static <T> DataVisitor<T> of(Class<T> type, String key){
		return new ClassBasedVisitor<>(type, key);
	}
}
