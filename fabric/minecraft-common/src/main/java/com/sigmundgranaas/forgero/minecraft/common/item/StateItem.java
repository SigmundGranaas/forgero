package com.sigmundgranaas.forgero.minecraft.common.item;

import com.sigmundgranaas.forgero.core.customdata.DataContainer;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.util.match.Context;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import com.sigmundgranaas.forgero.minecraft.common.conversion.CachedConverter;

import net.minecraft.item.ItemStack;

public interface StateItem extends DynamicAttributeItem, State {
	State defaultState();

	default State dynamicState(ItemStack stack) {
		return CachedConverter.of(stack).orElse(defaultState());
	}

	@Override
	default PropertyContainer dynamicProperties(ItemStack stack) {
		return dynamicState(stack);
	}

	@Override
	default PropertyContainer defaultProperties() {
		return defaultState();
	}

	default State state(ItemStack stack) {
		return CachedConverter.of(stack).orElse(defaultState());
	}


	default String name() {
		return defaultState().name();
	}

	default String nameSpace() {
		return defaultState().nameSpace();
	}

	default Type type() {
		return defaultState().type();
	}

	default boolean test(Matchable match, Context context) {
		return defaultState().test(match, context);
	}

	@Override
	default DataContainer customData() {
		return defaultState().customData();
	}
}
