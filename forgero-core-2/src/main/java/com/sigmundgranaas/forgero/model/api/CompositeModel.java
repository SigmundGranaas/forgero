package com.sigmundgranaas.forgero.model.api;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import java.util.List;
import java.util.Optional;

public record CompositeModel(OpenIdentifier identifier, List<Self> self, List<Part> parts, List<Part> upgrades, List<ModelSelector> modelSelectors, JsonElement display, List<JsonElement> overrides) implements Model {
	@Override public OpenIdentifier getIdentifier() { return identifier; }
	public record Self(String texture, int order) {}
	public record Part(String slot, int order) {}
	public record ModelSelector(String targetSlot, Optional<String> targetTag, String model) {}
}
