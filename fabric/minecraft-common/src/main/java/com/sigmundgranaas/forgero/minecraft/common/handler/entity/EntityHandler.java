package com.sigmundgranaas.forgero.minecraft.common.handler.entity;

import com.sigmundgranaas.forgero.core.property.Attribute;
import com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute;
import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;
import com.sigmundgranaas.forgero.minecraft.common.feature.FeatureUtils;

import net.minecraft.entity.Entity;

@FunctionalInterface
public interface EntityHandler {
	EntityHandler DEFAULT = (entity) -> {
	};
	ClassKey<EntityHandler> KEY = new ClassKey<>("minecraft:entity_handler", EntityHandler.class);

	default ComputedAttribute compute(Attribute base, Entity source) {
		return FeatureUtils.compute(base, source);
	}

	void handle(Entity entity);
}
