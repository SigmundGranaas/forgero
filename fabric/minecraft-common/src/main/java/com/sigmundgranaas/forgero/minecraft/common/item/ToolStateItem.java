package com.sigmundgranaas.forgero.minecraft.common.item;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;

import net.minecraft.item.ItemStack;

public interface ToolStateItem extends StateItem, DynamicAttributeItem {
	@Override
	default PropertyContainer dynamicProperties(ItemStack stack) {
		return dynamicState(stack);
	}

	@Override
	default PropertyContainer defaultProperties() {
		return defaultState();
	}
}
