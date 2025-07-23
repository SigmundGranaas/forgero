package com.sigmundgranaas.forgero.model.registry.api.armor;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.api.armor.ArmorModel;

import java.util.List;
import java.util.Optional;

public interface ArmorModelRegistry {
	Optional<ArmorModel> find(OpenIdentifier id);

	void register(ArmorModel model);

	List<ArmorModel> findAll();
}
