package com.sigmundgranaas.forgero.minecraft.common.handler.afterUse;

import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public interface AfterUseHandler {
	AfterUseHandler DEFAULT = (Entity source, ItemStack target, Hand hand) -> {
	};
	String AFTER_USE = "after_use";
	ClassKey<AfterUseHandler> KEY = new ClassKey<>("minecraft:after_use", AfterUseHandler.class);

	void handle(Entity source, ItemStack target, Hand hand);
}
