package com.sigmundgranaas.forgero.core.customdata;

import java.util.List;
import java.util.Optional;

/**
 * A visitor for {@link DataContainer}s.
 * <p>
 * This interface is used to check if a {@link DataContainer} contains a specific type of data.
 * Used to extract custom_data declared in resource files.
 * <p>
 * Example:
 * <pre>
 *     {@code
 *     "custom_data": {
 *          "ingredient_count": 2,
 *          "better_compat_attribute_container": "bettercombat:cutlass"
 *          }
 *     }
 * </pre>
 *
 * @param <T> the type of data to visit
 *            The visitors support primitive types, lists as well as custom classes.
 */
public interface DataVisitor<T> {
	static <T> DataVisitor<T> of(Class<T> type, String key) {
		return new ClassBasedVisitor<>(type, key);
	}

	Optional<T> visit(DataContainer dataContainer);

	List<T> visitMultiple(DataContainer dataContainer);

	String key();
}
