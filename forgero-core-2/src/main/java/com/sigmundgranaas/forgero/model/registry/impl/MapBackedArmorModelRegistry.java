// forgero-core-2/src/main/java/com/sigmundgranaas/forgero/model/armor/registry/impl/MapBackedArmorModelRegistry.java
package com.sigmundgranaas.forgero.model.registry.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.api.armor.ArmorModel;
import com.sigmundgranaas.forgero.model.registry.api.armor.ArmorModelRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class MapBackedArmorModelRegistry implements ArmorModelRegistry {
	private final Map<OpenIdentifier, ArmorModel> models = new ConcurrentHashMap<>();

	@Override
	public Optional<ArmorModel> find(OpenIdentifier id) {
		return Optional.ofNullable(models.get(id));
	}

	@Override
	public void register(ArmorModel model) {
		models.put(model.identifier(), model);
	}

	@Override
	public List<ArmorModel> findAll() {
		return new ArrayList<>(models.values());
	}
}
