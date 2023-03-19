package com.sigmundgranaas.forgero.core.customdata;

import java.util.Optional;

public interface Visitor<T> {
	Optional<T> visit(Visitable visitable);
}
