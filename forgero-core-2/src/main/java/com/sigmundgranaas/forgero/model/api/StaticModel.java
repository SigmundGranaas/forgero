package com.sigmundgranaas.forgero.model.api;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import java.util.Optional;

public record StaticModel(OpenIdentifier identifier, String texture, JsonElement display) implements Model {
	@Override public OpenIdentifier getIdentifier() { return identifier; }
	public Optional<JsonElement> getDisplay() { return Optional.ofNullable(display); }
}
