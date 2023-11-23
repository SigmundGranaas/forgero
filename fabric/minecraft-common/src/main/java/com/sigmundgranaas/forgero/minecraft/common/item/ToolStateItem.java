package com.sigmundgranaas.forgero.minecraft.common.item;

import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ToolStateItem extends StateItem, DynamicAttributeItem, DynamicUseHandler {
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
