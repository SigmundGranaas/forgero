package com.sigmundgranaas.forgero.core.customdata;

import java.util.Optional;

public interface Visitable {
	default <T> Optional<T> accept(Visitor<T> visitor){
		return visitor.visit(this);
	}
}
