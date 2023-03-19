package com.sigmundgranaas.forgero.core.customdata;

import java.util.Optional;

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
