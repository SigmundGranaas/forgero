package com.sigmundgranaas.forgero.minecraft.common.item;

import com.sigmundgranaas.forgero.core.state.StateProvider;

import com.sigmundgranaas.forgero.minecraft.common.registry.GenericRegistry;
import com.sigmundgranaas.forgero.minecraft.common.registry.RankableConverter;

import net.minecraft.item.ItemGroup;

public class ItemRegistries {
	public static final GenericRegistry<RankableConverter<StateProvider, ItemData>> STATE_CONVERTER = new GenericRegistry<>();
	public static final GenericRegistry<RankableConverter<StateProvider, ItemGroup>> GROUP_CONVERTER = new GenericRegistry<>();
	public static final GenericRegistry<SettingProcessor> SETTING_PROCESSOR = new GenericRegistry<>();
}
