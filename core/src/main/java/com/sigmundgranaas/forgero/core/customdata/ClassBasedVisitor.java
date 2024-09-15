package com.sigmundgranaas.forgero.core.customdata;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A {@link DataVisitor} that visits a {@link DataContainer} and returns the value of the key
 * This class uses Class#isInstance to check if the value is of the correct type in the resource.
 *
 * @param <T> The type of the value you want to extract
 */
public class ClassBasedVisitor<T> implements DataVisitor<T> {
	private final Class<T> type;
	private final String key;

	public ClassBasedVisitor(Class<T> type, String key) {
		this.type = type;
		this.key = key;
	}

	/**
	 * Visits a container and returns the value of the key if it is of the correct type.
	 * Will also check if the value is a list and return the first element if it is of the correct type.
	 *
	 * @param dataContainer the container to visit
	 * @return the value of the key if it is of the correct type
	 */
	@Override
	public Optional<T> visit(DataContainer dataContainer) {
		if (dataContainer instanceof CustomJsonDataContainer customJsonDataContainer) {
			return customJsonDataContainer.getObject(key(), type);
		}
		return visitMultiple(dataContainer).stream().findFirst();
	}

	/**
	 * Visits a container and returns all values of the key if they are of the correct type.
	 *
	 * @param dataContainer the container to visit
	 * @return all values of the key if they are of the correct type
	 */
	@Override
	public List<T> visitMultiple(DataContainer dataContainer) {
		if (dataContainer instanceof CustomJsonDataContainer customJsonDataContainer) {
			List<T> results = customJsonDataContainer.getObjectList(key(), type);
			if (results.isEmpty()) {
				visit(dataContainer).ifPresent(results::add);
			}
			return results;
		}
		return Collections.emptyList();
	}

	@Override
	public String key() {
		return key;
	}
}
