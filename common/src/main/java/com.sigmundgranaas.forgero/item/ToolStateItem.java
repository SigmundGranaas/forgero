package com.sigmundgranaas.forgero.item;

import java.util.List;

import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import org.jetbrains.annotations.NotNull;

import net.minecraft.item.ItemStack;

public interface ToolStateItem extends StateItem, DynamicAttributeItem, DynamicItemUseHandler {
	@Override
	default PropertyContainer dynamicProperties(ItemStack stack) {
		return dynamicState(stack);
	}

	@Override
	default PropertyContainer defaultProperties() {
		return defaultState();
	}

	@Override
	default @NotNull List<Property> getRootProperties(Matchable target, MatchContext context) {
		return defaultState().getRootProperties(target, context);
	}

}
