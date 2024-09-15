package com.sigmundgranaas.forgero.trident.item;

import com.sigmundgranaas.forgero.core.registry.GenericRegistry;
import com.sigmundgranaas.forgero.core.registry.RankableConverter;
import com.sigmundgranaas.forgero.core.registry.Registerable;
import com.sigmundgranaas.forgero.core.state.StateProvider;
import com.sigmundgranaas.forgero.minecraft.common.item.BuildableStateConverter;
import com.sigmundgranaas.forgero.minecraft.common.item.ItemData;

import net.minecraft.item.Item;

import static com.sigmundgranaas.forgero.minecraft.common.item.RegistryUtils.typeMatcher;

public class DynamicTridentRegistrationHandler implements Registerable<RankableConverter<StateProvider, ItemData>> {
	private final BuildableStateConverter defaultStateConverter;

	public DynamicTridentRegistrationHandler(BuildableStateConverter defaultStateConverter) {
		this.defaultStateConverter = defaultStateConverter;
	}

	@Override
	public void register(GenericRegistry<RankableConverter<StateProvider, ItemData>> registry) {
		registry.register("forgero:trident", defaultStateConverter.toBuilder()
				.priority(2)
				.matcher(typeMatcher("TRIDENT"))
				.item(this::trident)
				.build());

	}

	private Item trident(StateProvider provider, Item.Settings settings) {
		return new DynamicTridentItem(settings, provider);
	}
}
