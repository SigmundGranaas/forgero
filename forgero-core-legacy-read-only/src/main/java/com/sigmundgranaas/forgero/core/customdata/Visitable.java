package com.sigmundgranaas.forgero.core.customdata;

import java.util.Optional;

/**
 * A visitable object. Will be used to provide a visitor pattern for custom data to keep central domain logic flexible.
 * Classes can visit other classes to extract data, and handle them using custom logic to avoid tight coupling.
 */
public interface Visitable {
	default <T> Optional<T> accept(Visitor<T> visitor) {
		return visitor.visit(this);
	}
}
