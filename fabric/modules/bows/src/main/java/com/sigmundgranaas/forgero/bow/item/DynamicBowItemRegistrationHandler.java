package com.sigmundgranaas.forgero.bow.item;

import static com.sigmundgranaas.forgero.minecraft.common.item.RegistryUtils.typeMatcher;

import com.sigmundgranaas.forgero.core.state.StateProvider;
import com.sigmundgranaas.forgero.minecraft.common.item.BuildableStateConverter;
import com.sigmundgranaas.forgero.minecraft.common.item.GenericRegistry;
import com.sigmundgranaas.forgero.minecraft.common.item.ItemData;
import com.sigmundgranaas.forgero.minecraft.common.item.RankableConverter;
import com.sigmundgranaas.forgero.minecraft.common.item.Registerable;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

import net.minecraft.item.Item;

public class DynamicBowItemRegistrationHandler implements Registerable<RankableConverter<StateProvider, ItemData>> {
	private final BuildableStateConverter defaultStateConverter;

	public DynamicBowItemRegistrationHandler(BuildableStateConverter defaultStateConverter) {
		this.defaultStateConverter = defaultStateConverter;
	}

	@Override
	public void register(GenericRegistry<RankableConverter<StateProvider, ItemData>> registry) {
		var base = defaultStateConverter.toBuilder()
				.priority(1)
				.build();

		registry.register("forgero:bow", base.toBuilder()
				.priority(2)
				.matcher(typeMatcher("BOW"))
				.item(this::bow)
				.build());

		registry.register("forgero:arrow", base.toBuilder()
				.priority(2)
				.matcher(typeMatcher("ARROW"))
				.item(this::arrow)
				.build());
	}

	private Item bow(StateProvider provider, Item.Settings settings) {
		return new DynamicBowItem(settings, provider, StateService.INSTANCE);
	}

	private Item arrow(StateProvider provider, Item.Settings settings) {
		return new DynamicArrowItem(settings, provider);
	}
}
