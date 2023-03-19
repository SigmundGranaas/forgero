package com.sigmundgranaas.forgero.core.customdata;

import java.util.Optional;

/**
 * A visitor for {@link DataContainer}s.
 * <p>
 * Will check visitable objects for {@link DataSupplier} and return the custom data.
 */
public class ContainerVisitor implements Visitor<DataContainer> {
	public static ContainerVisitor VISITOR = new ContainerVisitor();

	@Override
	public Optional<DataContainer> visit(Visitable visitable) {
		if (visitable instanceof DataSupplier supplier) {
			return Optional.of(supplier.customData());
		}
		return Optional.empty();
	}
}
