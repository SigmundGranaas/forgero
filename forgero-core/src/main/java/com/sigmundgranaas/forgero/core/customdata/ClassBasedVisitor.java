package com.sigmundgranaas.forgero.core.customdata;

import java.util.Optional;

public class ClassBasedVisitor<T> implements DataVisitor<T>{
	private final Class<T> type;
	private final String key;

	public ClassBasedVisitor(Class<T> type, String key) {
		this.type = type;
		this.key = key;
	}

	@Override
	public Optional<T> visit(DataContainer dataContainer) {
		if(dataContainer instanceof CustomJsonDataContainer customJsonDataContainer){
			return customJsonDataContainer.getObject(key(), type);
		}
		return Optional.empty();
	}

	@Override
	public String key() {
		return key;
	}
}
