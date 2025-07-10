package com.sigmundgranaas.forgero.model.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import java.util.Optional;

public interface ModelRegistry {
	Optional<Model> find(OpenIdentifier id);
	void register(Model model);
}
