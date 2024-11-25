package com.sigmundgranaas.forgero.bow.item;

import com.sigmundgranaas.forgero.core.state.StateProvider;
import com.sigmundgranaas.forgero.core.registry.GenericRegistry;
import com.sigmundgranaas.forgero.core.registry.RankableConverter;
import com.sigmundgranaas.forgero.core.registry.Registerable;

import net.minecraft.item.ItemGroup;

public class BowGroupRegistrars implements Registerable<RankableConverter<StateProvider, ItemGroup>> {
	@Override
	public void register(GenericRegistry<RankableConverter<StateProvider, ItemGroup>> registry) {

	}
}
