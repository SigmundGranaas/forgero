package com.sigmundgranaas.forgero.core.resource.data.v2.data;


import com.sigmundgranaas.forgero.core.util.Identifiers;
import lombok.Builder;

import javax.annotation.Nullable;

import java.util.Objects;


@Builder(toBuilder = true)
public class IngredientData {
	@Builder.Default
	private final boolean unique = false;

	@Builder.Default
	private final int amount = 1;
	@Builder.Default
	@Nullable
	private final String type = Identifiers.EMPTY_IDENTIFIER;
	@Builder.Default
	@Nullable
	private final String id = Identifiers.EMPTY_IDENTIFIER;

	public boolean unique() {
		return unique;
	}

	public String type() {
		return Objects.requireNonNullElse(type, Identifiers.EMPTY_IDENTIFIER);
	}

	public String id() {
		return Objects.requireNonNullElse(id, Identifiers.EMPTY_IDENTIFIER);
	}

	public int amount() {
		return amount > 0 ? amount : 1;
	}

}
