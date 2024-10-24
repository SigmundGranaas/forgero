package com.sigmundgranaas.forgero.minecraft.common.item;

import com.sigmundgranaas.forgero.core.state.State;

import net.minecraft.item.Item;

@FunctionalInterface
public interface SettingProcessor {
	Item.Settings apply(Item.Settings settings, State state);
}
