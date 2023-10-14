package com.sigmundgranaas.forgero.minecraft.common.handler.entity;

import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;

import net.minecraft.entity.Entity;

@FunctionalInterface
public interface EntityHandler {
	EntityHandler DEFAULT = (entity) -> {
	};
	ClassKey<EntityHandler> KEY = new ClassKey<>("forgero:entity_handler", EntityHandler.class);

	void handle(Entity entity);
}
