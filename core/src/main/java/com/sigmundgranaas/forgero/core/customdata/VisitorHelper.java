package com.sigmundgranaas.forgero.core.customdata;

import static com.sigmundgranaas.forgero.core.customdata.ContainerVisitor.VISITOR;

import java.util.Optional;
import java.util.function.Supplier;

public class VisitorHelper {
	/**
	 * Accepts a visitor on a visitable object.
	 * <p>
	 * Will check visitable objects for {@link DataSupplier} and return the custom data.
	 *
	 * @param object  the object to visit
	 * @param visitor the visitor to accept
	 * @param <T>     the type of the visitor
	 * @return the result of the visitor
	 */
	public static <T> Optional<T> of(Object object, Supplier<DataVisitor<T>> visitor) {
		if (object instanceof Visitable visitable) {
			return visitable.accept(VISITOR).flatMap(container -> container.accept(visitor.get()));
		}
		return Optional.empty();
	}
}
