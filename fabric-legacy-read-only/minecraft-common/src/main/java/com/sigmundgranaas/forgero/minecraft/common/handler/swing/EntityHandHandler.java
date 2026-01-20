package com.sigmundgranaas.forgero.minecraft.common.handler.swing;

import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;

import net.minecraft.entity.Entity;
import net.minecraft.util.Hand;

public interface EntityHandHandler {
	String type = "minecraft:entity_hand";
	ClassKey<EntityHandHandler> KEY = new ClassKey<>(type, EntityHandHandler.class);
	EntityHandHandler DEFAULT = (Entity source, Hand hand) -> {
	};

	void onSwing(Entity source, Hand hand);
}
