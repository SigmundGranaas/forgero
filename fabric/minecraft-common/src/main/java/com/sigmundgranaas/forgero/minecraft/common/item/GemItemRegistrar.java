package com.sigmundgranaas.forgero.minecraft.common.item;

import static com.sigmundgranaas.forgero.minecraft.common.item.RegistryUtils.typeMatcher;

import com.sigmundgranaas.forgero.core.state.StateProvider;
import com.sigmundgranaas.forgero.core.registry.GenericRegistry;
import com.sigmundgranaas.forgero.core.registry.RankableConverter;
import com.sigmundgranaas.forgero.core.registry.Registerable;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

import net.minecraft.item.Item;

public class GemItemRegistrar implements Registerable<RankableConverter<StateProvider, ItemData>> {
	private final BuildableStateConverter defaultStateConverter;

	public GemItemRegistrar(BuildableStateConverter defaultStateConverter) {
		this.defaultStateConverter = defaultStateConverter;
	}

	@Override
	public void register(GenericRegistry<RankableConverter<StateProvider, ItemData>> registry) {
		var base = defaultStateConverter.toBuilder()
				.priority(1)
				.build();

		registry.register("forgero:gem", base.toBuilder()
				.matcher(typeMatcher("GEM"))
				.item(this::gem)
				.build());
	}

	private Item gem(StateProvider provider, Item.Settings settings) {
		return new GemItem(settings, provider.get(), StateService.INSTANCE);
	}
}
