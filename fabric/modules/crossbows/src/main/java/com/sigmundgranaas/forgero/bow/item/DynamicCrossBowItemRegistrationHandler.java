package com.sigmundgranaas.forgero.bow.item;

import static com.sigmundgranaas.forgero.minecraft.common.item.RegistryUtils.typeMatcher;

import com.sigmundgranaas.forgero.core.registry.GenericRegistry;
import com.sigmundgranaas.forgero.core.registry.RankableConverter;
import com.sigmundgranaas.forgero.core.registry.Registerable;
import com.sigmundgranaas.forgero.core.state.StateProvider;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.minecraft.common.item.BuildableStateConverter;
import com.sigmundgranaas.forgero.minecraft.common.item.ItemData;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

import net.minecraft.item.Item;

public class DynamicCrossBowItemRegistrationHandler implements Registerable<RankableConverter<StateProvider, ItemData>> {
	private final BuildableStateConverter defaultStateConverter;

	public DynamicCrossBowItemRegistrationHandler(BuildableStateConverter defaultStateConverter) {
		this.defaultStateConverter = defaultStateConverter;
	}

	@Override
	public void register(GenericRegistry<RankableConverter<StateProvider, ItemData>> registry) {
		var base = defaultStateConverter.toBuilder()
				.priority(1)
				.build();

		registry.register("forgero:crossbow", base.toBuilder()
				.priority(2)
				.matcher(typeMatcher(Type.CROSSBOW))
				.item(this::crossbow)
				.build());

	}

	private Item crossbow(StateProvider provider, Item.Settings settings) {
		return new DynamicCrossBowItem(settings, provider, StateService.INSTANCE);
	}
}
