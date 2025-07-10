package com.sigmundgranaas.forgero.model.registry.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.api.Model;

import java.util.Optional;

public interface ModelRegistry {
	Optional<Model> find(OpenIdentifier id);
	void register(Model model);
}
