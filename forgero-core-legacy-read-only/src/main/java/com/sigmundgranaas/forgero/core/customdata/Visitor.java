package com.sigmundgranaas.forgero.core.customdata;

import java.util.Optional;

/**
 * A visitor for {@link Visitable}s.
 * <p>
 * See ContainerVisitor for an example.
 *
 * @param <T> the type of data to visit
 */
public interface Visitor<T> {
	Optional<T> visit(Visitable visitable);
}
