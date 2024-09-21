package com.sigmundgranaas.forgero.item;

import com.sigmundgranaas.forgero.core.registry.GenericRegistry;
import com.sigmundgranaas.forgero.core.registry.RankableConverter;
import com.sigmundgranaas.forgero.core.state.StateProvider;

import net.minecraft.item.ItemGroup;

public class ItemRegistries {
	public static final GenericRegistry<RankableConverter<StateProvider, ItemData>> STATE_CONVERTER = new GenericRegistry<>();
	public static final GenericRegistry<RankableConverter<StateProvider, ItemGroup>> GROUP_CONVERTER = new GenericRegistry<>();
	public static final GenericRegistry<SettingProcessor> SETTING_PROCESSOR = new GenericRegistry<>();
}
